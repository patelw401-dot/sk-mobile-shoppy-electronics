import test from 'node:test';
import assert from 'node:assert/strict';
import worker from './index.js';
import {fields,unpack,encode} from './firestore.js';
import {googleAccessToken} from './auth.js';
const env={FIREBASE_PROJECT_ID:'test'};
test('health is explicitly not ready',async()=>{const r=await worker.fetch(new Request('https://example.com/health'),env);assert.equal(r.status,200);assert.equal((await r.json()).ready,false)});
test('missing token rejected',async()=>{assert.equal((await worker.fetch(new Request('https://example.com/auth-check'),env)).status,401)});
test('malformed token rejected',async()=>{assert.equal((await worker.fetch(new Request('https://example.com/auth-check',{headers:{Authorization:'Bearer invalid'}}),env)).status,401)});
test('unfinished order endpoint fails closed',async()=>{assert.equal((await worker.fetch(new Request('https://example.com/orders',{method:'POST'}),env)).status,503)});
test('Firestore field round trip',()=>{const v={stock:5,active:true,items:[{id:'a',quantity:2}],name:'test',empty:null};assert.deepEqual(unpack({fields:fields(v)}),v)});
test('unsupported numeric input rejected',()=>assert.throws(()=>encode(Infinity)));
test('missing server credentials rejected',async()=>assert.rejects(googleAccessToken({})));

import {createOrder,cancelOrder} from './orders.js';
function fakeDb(){
 const data=new Map([['products/p',{stock:5,price:100,inStock:true}]]);
 return {data,name:(c,i)=>c+'/'+i,async transaction(fn){
  const {writes,result}=await fn(async(c,i)=>data.has(c+'/'+i)?{fields:fields(data.get(c+'/'+i))}:null);
  for(const w of writes){const k=w.update.name;const v=unpack(w.update);data.set(k,w.updateMask?{...data.get(k),...v}:v)}
  return result;
 }};
}
const input={orderId:'o',customerName:'Test',phone:'1234567890',address:'Test address',items:[{id:'p',quantity:2,price:1}]};
test('server price and stock, idempotent order and cancellation',async()=>{
 const db=fakeDb();const o=await createOrder(db,'u',input);assert.equal(o.total,200);assert.equal(db.data.get('products/p').stock,3);
 await createOrder(db,'u',input);assert.equal(db.data.get('products/p').stock,3);
 await assert.rejects(cancelOrder(db,'other',{orderId:'o'}));
 await cancelOrder(db,'u',{orderId:'o'});assert.equal(db.data.get('products/p').stock,5);
 await cancelOrder(db,'u',{orderId:'o'});assert.equal(db.data.get('products/p').stock,5);
});
test('insufficient stock rejected without mutation',async()=>{const db=fakeDb();await assert.rejects(createOrder(db,'u',{...input,items:[{id:'p',quantity:6}]}));assert.equal(db.data.get('products/p').stock,5);assert.equal(db.data.has('orders/o'),false)});
