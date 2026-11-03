import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';

// Runs ONLY against loopback emulators. No production credentials or data.
assert(process.env.FIREBASE_DATABASE_EMULATOR_HOST === '127.0.0.1:29000');
assert(process.env.FIREBASE_AUTH_EMULATOR_HOST === '127.0.0.1:29099');
const ns = 'demo-afora-default-rtdb';
const db = `http://127.0.0.1:29000`;
const auth = 'http://127.0.0.1:29099/identitytoolkit.googleapis.com/v1/accounts:';
const rules = JSON.parse((await readFile(new URL('./database.rules.json', import.meta.url), 'utf8')).replace(/^\uFEFF/, ''));
const owner = {Authorization: 'Bearer owner', 'Content-Type':'application/json'};
async function request(url, method='GET', body, headers={}) {
    const response = await fetch(url, {method, headers: {'Content-Type':'application/json', ...headers}, body: body === undefined ? undefined : JSON.stringify(body)});
    return {status:response.status, data:await response.json(), headers:response.headers};
}
function path(p, token) { return `${db}/${p}.json?ns=${ns}${token ? '&auth='+encodeURIComponent(token):''}`; }
async function account(action, body) { return request(`${auth}${action}?key=fake-key`, 'POST', body); }
assert.equal((await request(`${db}/.settings/rules.json?ns=${ns}`, 'PUT', rules, owner)).status,200);
const stamp = Date.now();
const studentEmail = `student${stamp}@example.test`;
const password = 'Test-only-Password123!';
let student = await account('signUp',{email:studentEmail,password,displayName:'Estudiante AFORA',returnSecureToken:true});
assert.equal(student.status,200);
assert.equal((await account('update',{idToken:student.data.idToken,displayName:'Estudiante AFORA',returnSecureToken:true})).status,200);
assert.equal((await account('signUp',{email:studentEmail,password,returnSecureToken:true})).status,400);
assert.equal((await account('signInWithPassword',{email:studentEmail,password:'wrong-password',returnSecureToken:true})).status,400);
student = await account('signInWithPassword',{email:studentEmail,password,returnSecureToken:true});
assert.equal(student.status,200);

assert.equal((await account('lookup',{idToken:student.data.idToken})).data.users[0].displayName,'Estudiante AFORA');
const controller = await account('signUp',{email:`controller${stamp}@example.test`,password,returnSecureToken:true});
assert.equal(controller.status,200);
assert.equal((await request(path('controllers/'+controller.data.localId),'PUT',true,owner)).status,200);
const state = {currentPeople:0,maxCapacity:50,lastUpdated:Date.now()};
assert.equal((await request(path('aforo'),'PUT',state,owner)).status,200);
assert.equal((await request(path('aforo',student.data.idToken),'PUT',{...state,currentPeople:1})).status,401);
assert.equal((await request(path('aforo'),'PUT',{...state,currentPeople:1})).status,401);
assert.equal((await request(path('controllers/'+student.data.localId,student.data.idToken),'PUT',true)).status,401);
assert.equal((await request(path('aforo',controller.data.idToken),'PUT',{...state,currentPeople:-1})).status,401);
assert.equal((await request(path('aforo',controller.data.idToken),'PUT',{...state,currentPeople:51})).status,401);
assert.equal((await request(path('aforo',controller.data.idToken),'DELETE')).status,401);
async function move(delta) {
  const before = await request(path('aforo',controller.data.idToken),'GET',undefined,{'X-Firebase-ETag':'true'});
  const updated = {...before.data,currentPeople:before.data.currentPeople+delta,lastUpdated:Date.now()};
  assert.equal((await request(path('aforo',controller.data.idToken),'PUT',updated,{'if-match':before.headers.get('etag')})).status,200);
  const observed = await request(path('aforo',student.data.idToken));
  assert.equal(observed.data.currentPeople,updated.currentPeople);
  return before;
}
const stale = await move(1);
assert.equal((await request(path('aforo',controller.data.idToken),'PUT',state,{'if-match':stale.headers.get('etag')})).status,412);
await move(1); await move(-1);
assert.equal((await request(path('aforo'))).data.currentPeople,1);
// A fresh request after the controller stops still sees the persisted value.
assert.equal((await request(path('aforo',student.data.idToken))).data.currentPeople,1);
assert.equal((await request(path('history',student.data.idToken))).status,401);
assert.equal((await request(path('aforo',controller.data.idToken),'PUT',{...state,lastUpdated:Date.now()})).status,200);
assert.equal((await request(path('aforo'))).data.currentPeople,0);
console.log('PASS: registration, duplicate prevention, login/name, incorrect password, controller permissions, capacity bounds, reset, shared state, ETag conflicts and persistence between requests.');

// A passwordless tablet still needs explicit server authorization.
const tablet = await account('signUp',{returnSecureToken:true});
assert.equal(tablet.status,200);
assert.equal((await request(path('aforo',tablet.data.idToken),'PUT',{...state,currentPeople:1})).status,401);
assert.equal((await request(path('controllers/'+tablet.data.localId),'PUT',true,owner)).status,200);
assert.equal((await request(path('aforo',tablet.data.idToken),'PUT',{...state,currentPeople:1})).status,200);
const renewedResponse = await fetch('http://127.0.0.1:29099/securetoken.googleapis.com/v1/token?key=fake-key', {
  method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},
  body:new URLSearchParams({grant_type:'refresh_token',refresh_token:tablet.data.refreshToken})
});
assert.equal(renewedResponse.status,200);
const renewedTablet=await renewedResponse.json();
assert.equal((await account('lookup',{idToken:renewedTablet.id_token})).data.users[0].localId,tablet.data.localId);
assert.equal((await request(path('aforo',renewedTablet.id_token),'PUT',{...state,currentPeople:2})).status,200);
assert.equal((await request(path('aforo',student.data.idToken))).data.currentPeople,2);
assert.equal((await request(path('controllers/'+tablet.data.localId),'DELETE',undefined,owner)).status,200);
assert.equal((await request(path('aforo',renewedTablet.id_token),'PUT',state)).status,401);
console.log('PASS: passwordless tablet, denial before approval, approved writes, refresh preserving identity, shared count and revocation.');
