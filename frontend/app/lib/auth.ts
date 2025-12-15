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

interface UserData {
  token: string;
  userId: number;
  userName: string;
  userEmail: string;
  role: string;
  status: string;
}

export const saveUserData = (data: UserData) => {
  if (typeof window === 'undefined') return;

  localStorage.setItem("token", data.token);
  localStorage.setItem("userId", data.userId.toString());
  localStorage.setItem("userName", data.userName);
  localStorage.setItem("userEmail", data.userEmail);
  localStorage.setItem("userRole", data.role);
  localStorage.setItem("userStatus", data.status);

  // Store full user object for UserContext
  localStorage.setItem('user', JSON.stringify({
    id: data.userId,
    username: data.userName,
    email: data.userEmail,
    role: data.role,
    status: data.status
  }));

  // Dispatch custom event to notify other components
  window.dispatchEvent(new Event('userDataUpdated'));
};

/**
 * Clear all user data from localStorage and redirect to home
 */
export function logout() {
  if (typeof window === 'undefined') return;

  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('userName');
  localStorage.removeItem('userEmail');
  localStorage.removeItem('userRole');
  localStorage.removeItem('user');

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

/**
 * Get current user role from localStorage
 */
export function getUserRole(): string {
  if (typeof window === 'undefined') return 'USER';

  return localStorage.getItem('userRole') || 'USER';
}

/**
 * Check if current user is admin
 */
export function isAdmin(): boolean {
  return getUserRole() === 'ADMIN';
}
