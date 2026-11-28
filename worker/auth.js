const enc = new TextEncoder();
const b64 = bytes => btoa(String.fromCharCode(...new Uint8Array(bytes))).replace(/=/g,"").replace(/\+/g,"-").replace(/\//g,"_");
const decode = s => Uint8Array.from(atob(s.replace(/-/g,"+").replace(/_/g,"/").padEnd(Math.ceil(s.length/4)*4,"=")),c=>c.charCodeAt(0));
const json = s => JSON.parse(new TextDecoder().decode(decode(s)));
export async function verifyFirebaseToken(request, env) {
  const token = request.headers.get("Authorization")?.match(/^Bearer ([^ ]+)$/i)?.[1];
  if (!token || token.length > 16000) throw new Error("Unauthorized");
  const parts = token.split(".");
  if(parts.length !== 3) throw new Error("Unauthorized");
  const [h,p,s] = parts, header=json(h), claims=json(p);
  const now=Math.floor(Date.now()/1000), project=env.FIREBASE_PROJECT_ID;
  if(header.alg!=="RS256" || typeof header.kid!=="string" || claims.aud!==project || claims.iss!=="https://securetoken.google.com/"+project || typeof claims.sub!=="string" || !claims.sub || claims.sub.length>128 || !Number.isInteger(claims.exp) || claims.exp<=now || !Number.isInteger(claims.iat) || claims.iat>now || !Number.isInteger(claims.auth_time) || claims.auth_time>now) throw new Error("Unauthorized");
  const response=await fetch("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com");
  if(!response.ok) throw new Error("Authentication unavailable");
  const jwk=(await response.json()).keys.find(k=>k.kid===header.kid && k.kty==="RSA");
  if(!jwk) throw new Error("Unauthorized");
  const key=await crypto.subtle.importKey("jwk",jwk,{name:"RSASSA-PKCS1-v1_5",hash:"SHA-256"},false,["verify"]);
  if(!await crypto.subtle.verify("RSASSA-PKCS1-v1_5",key,decode(s),enc.encode(h+"."+p))) throw new Error("Unauthorized");
  if(claims.firebase?.sign_in_provider==="password" && claims.email_verified!==true) throw new Error("Verify email");
  return claims;
}
export async function googleAccessToken(env) {
  const email=env.GOOGLE_SERVICE_ACCOUNT_EMAIL, pem=env.GOOGLE_PRIVATE_KEY;
  if(!email || !pem) throw new Error("Server credentials unavailable");
  const now=Math.floor(Date.now()/1000);
  const head=b64(enc.encode(JSON.stringify({alg:"RS256",typ:"JWT"})));
  const body=b64(enc.encode(JSON.stringify({iss:email,scope:"https://www.googleapis.com/auth/datastore",aud:"https://oauth2.googleapis.com/token",iat:now,exp:now+300})));
  const key=await crypto.subtle.importKey("pkcs8",decode(pem.replace(/-----[^-]+-----/g,"").replace(/\s/g,"")),{name:"RSASSA-PKCS1-v1_5",hash:"SHA-256"},false,["sign"]);
  const assertion=head+"."+body+"."+b64(await crypto.subtle.sign("RSASSA-PKCS1-v1_5",key,enc.encode(head+"."+body)));
  const res=await fetch("https://oauth2.googleapis.com/token",{method:"POST",body:new URLSearchParams({grant_type:"urn:ietf:params:oauth:grant-type:jwt-bearer",assertion})});
  if(!res.ok) throw new Error("Server authentication unavailable");
  const data=await res.json();
  if(typeof data.access_token!=="string") throw new Error("Server authentication unavailable");
  return data.access_token;
}
