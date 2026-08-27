import React, { useState, useEffect } from 'react';
import { User } from './types/user';
import { authService } from './services/authService';
import { LoginModal } from './components/LoginModal';
import { Dashboard } from './pages/Dashboard';

export const App: React.FC = () => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);

  useEffect(() => {
    const user = authService.getStoredUser();
    if (user) {
      setCurrentUser(user);
    }
  }, []);

  if (!currentUser) {
    return <LoginModal onLoginSuccess={(user) => setCurrentUser(user)} />;
  }

  return <Dashboard user={currentUser} />;
};

export default App;
