// PolyChat Mobile — Dynamic data from Neon Database
// These arrays start empty and get populated from the API

let MOCK_USERS = [];
let MOCK_CHATS = [];
let MOCK_STORIES = [];
let MOCK_CONTACTS = [];
let MOCK_REQUESTS = [];

const API_BASE = '';  // Same origin (served by server.js)

// App state
const state = {
  currentUser: null,
  currentScreen: 'login',
  currentChat: null,
  pendingPhone: '',
  verifyCode: '123456',
  theme: 'dark',
  bgIndex: 0,
  showOriginal: {},
  settingsTab: 'profile',
  dataLoaded: false
};

// ===================== API FUNCTIONS =====================

async function apiGet(endpoint) {
  try {
    const res = await fetch(API_BASE + endpoint);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.error(`API error [${endpoint}]:`, err);
    return null;
  }
}

// Load all users from DB
async function loadUsers() {
  const users = await apiGet('/api/users');
  if (users) MOCK_USERS = users;
}

// Login by phone number — returns user object from DB
async function loginByPhone(phone) {
  const user = await apiGet('/api/login?phone=' + encodeURIComponent(phone));
  return user;
}

// Load chats for the logged-in user
async function loadChats(userId) {
  const chats = await apiGet('/api/chats?userId=' + userId);
  if (chats) MOCK_CHATS = chats;
}

// Load contacts for the logged-in user
async function loadContacts(userId) {
  const contacts = await apiGet('/api/contacts?userId=' + userId);
  if (contacts) MOCK_CONTACTS = contacts;
}

// Load stories
async function loadStories(userId) {
  const stories = await apiGet('/api/stories?userId=' + userId);
  if (stories) MOCK_STORIES = stories;
}

// Load ALL data for a logged-in user
async function loadAllData(userId) {
  await Promise.all([
    loadUsers(),
    loadChats(userId),
    loadContacts(userId),
    loadStories(userId)
  ]);
  state.dataLoaded = true;
}
