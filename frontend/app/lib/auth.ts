/**
 * Authentication utility functions
 */

/**
 * Get authentication headers for API requests
 */
export function getAuthHeaders(): HeadersInit {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  
  return {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` })
  };
}

/**
 * Save user data to localStorage after successful login
 */
export function saveUserData(userData: {
  token: string;
  userId: number;
  userName?: string;
  userEmail?: string;
}) {
  if (typeof window === 'undefined') return;
  
  localStorage.setItem('token', userData.token);
  localStorage.setItem('userId', userData.userId.toString());
  
  if (userData.userName) {
    localStorage.setItem('userName', userData.userName);
  }
  
  if (userData.userEmail) {
    localStorage.setItem('userEmail', userData.userEmail);
  }

  // Dispatch custom event to notify other components
  window.dispatchEvent(new Event('userDataUpdated'));
}

/**
 * Clear all user data from localStorage and redirect to home
 */
export function logout() {
  if (typeof window === 'undefined') return;
  
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('userName');
  localStorage.removeItem('userEmail');
  
  window.location.href = '/';
}

/**
 * Check if user is authenticated
 */
export function isAuthenticated(): boolean {
  if (typeof window === 'undefined') return false;
  
  const token = localStorage.getItem('token');
  return !!token;
}

/**
 * Get current user ID from localStorage
 */
export function getCurrentUserId(): number | null {
  if (typeof window === 'undefined') return null;
  
  const userId = localStorage.getItem('userId');
  return userId ? parseInt(userId, 10) : null;
}

/**
 * Get current user name from localStorage
 */
export function getCurrentUserName(): string | null {
  if (typeof window === 'undefined') return null;
  
  return localStorage.getItem('userName');
}

/**
 * Get current user email from localStorage
 */
export function getCurrentUserEmail(): string | null {
  if (typeof window === 'undefined') return null;
  
  return localStorage.getItem('userEmail');
}
