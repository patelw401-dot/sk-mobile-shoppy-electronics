import {verifyFirebaseToken} from "./auth.js";
export default {
 async fetch(request,env) {
  const url=new URL(request.url);
  const headers={"Content-Type":"application/json","Cache-Control":"no-store","X-Content-Type-Options":"nosniff"};
  const reply=(data,status=200)=>new Response(JSON.stringify(data),{status,headers});
  if(request.method==="GET" && url.pathname==="/health")return reply({status:"development",ready:false});
  if(request.method==="GET" && url.pathname==="/auth-check"){
   try {await verifyFirebaseToken(request,env);return reply({authenticated:true});}
   catch{return reply({error:"Authentication failed"},401);}
  }
  return reply({error:"Backend not enabled"},503);
 }
};
