'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export function useAuth() {
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem('token');
    
    if (!token) {
      router.push('/login');
    }
  }, [router]);

  return {
    token: typeof window !== 'undefined' ? localStorage.getItem('token') : null,
    userId: typeof window !== 'undefined' ? localStorage.getItem('userId') : null,
    userName: typeof window !== 'undefined' ? localStorage.getItem('userName') : null,
    userEmail: typeof window !== 'undefined' ? localStorage.getItem('userEmail') : null,
  };
}

export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('userName');
  localStorage.removeItem('userEmail');
  localStorage.removeItem('user');
  window.location.href = '/login';
}

export function getAuthHeaders() {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  
  if (!token) {
    return {};
  }

  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
}

export function saveUserData(userData: { id: number; username: string; email: string; token: string }) {
  localStorage.setItem('token', userData.token);
  localStorage.setItem('userId', userData.id.toString());
  localStorage.setItem('userName', userData.username);
  localStorage.setItem('userEmail', userData.email);
  localStorage.setItem('user', JSON.stringify({
    id: userData.id,
    username: userData.username,
    email: userData.email,
  }));
}
