export default {
  async fetch(request) {
    const url = new URL(request.url);
    const headers = {"Content-Type":"application/json","Cache-Control":"no-store","X-Content-Type-Options":"nosniff"};
    if (request.method === "GET" && url.pathname === "/health") {
      return new Response(JSON.stringify({status:"development",ready:false}),{headers});
    }
    return new Response(JSON.stringify({error:"Backend not enabled"}),{status:503,headers});
  }
};
