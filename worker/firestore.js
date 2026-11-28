import {googleAccessToken} from "./auth.js";
export async function firestore(env) {
 const token=await googleAccessToken(env);
 const root="projects/"+env.FIREBASE_PROJECT_ID+"/databases/"+env.FIRESTORE_DATABASE;
 const base="https://firestore.googleapis.com/v1/"+root+"/documents";
 async function call(path,body) {
  const r=await fetch(base+path,{method:"POST",headers:{Authorization:"Bearer "+token,"Content-Type":"application/json"},body:JSON.stringify(body)});
  if(!r.ok) throw new Error("Firestore request failed: "+r.status);
  return r.json();
 }
 const name=(collection,id)=>root+"/documents/"+collection+"/"+id;
 return {name,async transaction(fn){
  const {transaction}=await call(":beginTransaction",{options:{readWrite:{}}});
  try {
   const get=async(collection,id)=>{
    const rows=await call(":batchGet",{documents:[name(collection,id)],transaction});
    return rows[0]?.found||null;
   };
   const {writes,result}=await fn(get);
   await call(":commit",{transaction,writes});
   return result;
  }catch(e){try{await call(":rollback",{transaction});}catch{}throw e;}
 }};
}
export function encode(v){
 if(v===null)return {nullValue:null};
 if(typeof v==="string")return {stringValue:v};
 if(typeof v==="boolean")return {booleanValue:v};
 if(Number.isSafeInteger(v))return {integerValue:String(v)};
 if(Array.isArray(v))return {arrayValue:{values:v.map(encode)}};
 if(v && typeof v==="object")return {mapValue:{fields:fields(v)}};
 throw new Error("Unsupported field");
}
export const fields=o=>Object.fromEntries(Object.entries(o).map(([k,v])=>[k,encode(v)]));
export function decode(v){
 if("stringValue"in v)return v.stringValue;
 if("integerValue"in v)return Number(v.integerValue);
 if("doubleValue"in v)return v.doubleValue;
 if("booleanValue"in v)return v.booleanValue;
 if("nullValue"in v)return null;
 if("arrayValue"in v)return (v.arrayValue.values||[]).map(decode);
 if("mapValue"in v)return unpack(v.mapValue);
 return undefined;
}
export const unpack=d=>Object.fromEntries(Object.entries(d?.fields||{}).map(([k,v])=>[k,decode(v)]));
