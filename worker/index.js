import {verifyFirebaseToken} from "./auth.js";
import {firestore} from "./firestore.js";
import {createOrder,cancelOrder} from "./orders.js";
export default {
 async fetch(request,env) {
  const url=new URL(request.url);
  const headers={"Content-Type":"application/json","Cache-Control":"no-store","X-Content-Type-Options":"nosniff"};
  const origin=request.headers.get("Origin");
  const allowed=env.STAGING_WEB_ORIGIN;
  if(origin && origin!==allowed)return new Response(null,{status:403});
  if(origin){headers["Access-Control-Allow-Origin"]=origin;headers["Vary"]="Origin";}
  if(request.method==="OPTIONS"){
   if(!origin || !["/orders","/orders/cancel","/auth-check"].includes(url.pathname))return new Response(null,{status:403});
   return new Response(null,{status:204,headers:{...headers,"Access-Control-Allow-Methods":"GET, POST, OPTIONS","Access-Control-Allow-Headers":"Authorization, Content-Type","Access-Control-Max-Age":"600"}});
  }
  const reply=(data,status=200)=>new Response(JSON.stringify(data),{status,headers});
  if(request.method==="GET" && url.pathname==="/health")return reply({status:"development",ready:false});
  const orderRoute=request.method==="POST" && ["/orders","/orders/cancel"].includes(url.pathname);
  if(orderRoute && env.ENABLE_STAGING_ORDERS!=="true")return reply({error:"Backend not enabled"},503);
  if(orderRoute || (request.method==="GET" && url.pathname==="/auth-check")){
   let claims;
   try {claims=await verifyFirebaseToken(request,env);}catch{return reply({error:"Authentication failed"},401);}
   if(!orderRoute)return reply({authenticated:true});
   if(!env.STAGING_FIREBASE_PROJECT_ID || env.FIREBASE_PROJECT_ID!==env.STAGING_FIREBASE_PROJECT_ID || env.FIREBASE_PROJECT_ID==="gen-lang-client-0232823199")return reply({error:"Staging configuration required"},503);
   try {
    const raw=await request.text();
    if(raw.length>65536)return reply({error:"Request too large"},413);
    const input=JSON.parse(raw);
    const db=await firestore(env);
    const result=await (url.pathname==="/orders"?createOrder:cancelOrder)(db,claims.sub,input);
    return reply({data:result});
   }catch{return reply({error:"Order operation failed"},400);}
  }
  return reply({error:"Backend not enabled"},503);
 }
};
