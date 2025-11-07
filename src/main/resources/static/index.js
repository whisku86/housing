// Modal Logic
const openAuth = document.getElementById('openAuth');
const closeAuth = document.getElementById('closeAuth');
const authModal = document.getElementById('authModal');

openAuth.addEventListener('click', () => authModal.style.display = 'flex');
closeAuth.addEventListener('click', () => authModal.style.display = 'none');
window.addEventListener('click', (e) => {
  if (e.target === authModal) authModal.style.display = 'none';
});

// Tab Switch Logic
const signInTab = document.getElementById('signInTab');
const signUpTab = document.getElementById('signUpTab');
const signInForm = document.getElementById('signInForm');
const signUpForm = document.getElementById('signUpForm');

signInTab.addEventListener('click', () => {
  signInForm.classList.remove('hidden');
  signUpForm.classList.add('hidden');
  signInTab.classList.add('active');
  signUpTab.classList.remove('active');
});

signUpTab.addEventListener('click', () => {
  signUpForm.classList.remove('hidden');
  signInForm.classList.add('hidden');
  signUpTab.classList.add('active');
  signInTab.classList.remove('active');
});
