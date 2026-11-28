import {fields,unpack,encode} from "./firestore.js";
const id=v=>{if(typeof v!=="string"||! /^[\w.-]{1,128}$/.test(v)||v==="."||v==="..")throw new Error("Invalid identifier");return v};
const text=(v,n)=>typeof v==="string"?v.trim().slice(0,n):"";
const patch=(name,data)=>({update:{name,fields:fields(data)},updateMask:{fieldPaths:Object.keys(data)},currentDocument:{exists:true}});
export async function createOrder(db,uid,input){
 const orderId=id(input.orderId), customerName=text(input.customerName,120),phone=text(input.phone,30),address=text(input.address,600);
 if(!customerName||phone.replace(/\D/g,"").length<10||!address||!Array.isArray(input.items)||!input.items.length||input.items.length>50)throw new Error("Invalid order");
 const quantities=new Map();
 const requested=input.items.map(i=>{const key=id(i.id),q=i.quantity;if(!Number.isSafeInteger(q)||q<1||q>99)throw new Error("Invalid quantity");quantities.set(key,(quantities.get(key)||0)+q);return {id:key,quantity:q,color:text(i.color,80)}});
 return db.transaction(async get=>{
  const existing=await get("orders",orderId);
  if(existing){const o=unpack(existing);if(o.customerId!==uid)throw new Error("Order unavailable");return {writes:[],result:o}}
  const products=new Map(),writes=[];
  for(const [key,q] of quantities){
   const doc=await get("products",key),p=unpack(doc);
   if(!doc||p.active===false||p.isActive===false||p.inStock===false||!Number.isSafeInteger(p.stock)||p.stock<q||!Number.isSafeInteger(p.price)||p.price<1)throw new Error("Product unavailable");
   products.set(key,p);writes.push(patch(db.name("products",key),{stock:p.stock-q,inStock:p.stock>q}));
  }
  const items=requested.map(i=>{const p=products.get(i.id);return {...i,price:p.price,subtotal:p.price*i.quantity,name:p.name||"Product",imageUrl:p.imageUrl||p.primaryImage||"",brand:p.brand||"",category:p.category||""}});
  const total=items.reduce((s,i)=>s+i.subtotal,0);if(!Number.isSafeInteger(total)||total>2147483647)throw new Error("Invalid total");
  const now=Date.now(),pinCode=text(input.pinCode,12),deliveryMethod=text(input.deliveryMethod,140);
  const order={id:orderId,orderId,customerId:uid,customerName,phone,address,pinCode,pincode:pinCode,city:text(input.city,100),state:text(input.state,100),deliveryMethod,deliveryType:deliveryMethod,notes:text(input.notes,800),paymentMethod:input.paymentMethod==="UPI / QR Code on Delivery"?input.paymentMethod:"Cash on Delivery",paymentStatus:"Pending",idempotencyKey:orderId,orderStatus:"Order Placed",status:"Order Placed",orderDate:new Intl.DateTimeFormat("en-IN",{dateStyle:"medium",timeZone:"Asia/Kolkata"}).format(now),items,subtotal:total,total,deliveryCharge:0,stockTracking:true,stockDeducted:true,createdAt:now,updatedAt:now,statusHistory:[{status:"Order Placed",timestamp:now,note:"Order placed",updatedBy:"Customer"}]};
  writes.push({update:{name:db.name("orders",orderId),fields:fields(order)},currentDocument:{exists:false}});
  return {writes,result:order};
 });
}
export async function cancelOrder(db,uid,input){
 const key=id(input.orderId),reason=text(input.reason,500);
 return db.transaction(async get=>{
  const doc=await get("orders",key),o=unpack(doc);
  if(!doc||o.customerId!==uid)throw new Error("Order unavailable");
  const status=o.orderStatus||o.status;
  if(status==="Cancelled")return {writes:[],result:{cancelled:true}};
  if(status==="Delivered")throw new Error("Delivered order");
  const writes=[],quantities=new Map();
  if(o.stockDeducted===true){
   for(const i of o.items){const k=id(i.id);if(!Number.isSafeInteger(i.quantity)||i.quantity<1)throw new Error("Invalid stored quantity");quantities.set(k,(quantities.get(k)||0)+i.quantity)}
   for(const [k,q] of quantities){const p=await get("products",k);if(!p)throw new Error("Missing product");const stock=unpack(p).stock;if(!Number.isSafeInteger(stock)||stock<0||!Number.isSafeInteger(stock+q))throw new Error("Invalid stock");writes.push(patch(db.name("products",k),{stock:stock+q,inStock:true}))}
  }
  const now=Date.now();writes.push(patch(db.name("orders",key),{orderStatus:"Cancelled",status:"Cancelled",cancellationReason:reason,updatedAt:now,stockDeducted:false,statusHistory:[...(o.statusHistory||[]),{status:"Cancelled",timestamp:now,note:reason,updatedBy:"Customer"}]}));
  return {writes,result:{cancelled:true}};
 });
}
