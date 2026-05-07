import { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import NotFound from './pages/NotFound';

import './App.css'

function App() {
  const [gamesCount, setGamesCount] = useState(0);

  const updateGamesCount = () => {
    setGamesCount(gamesCount + 1);
    console.log('Game won. Incrementing games count');
  }

  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route index element={ <Dashboard gamesCount={ gamesCount } updateGamesCount={ updateGamesCount } /> } />
          <Route path="*" element={ <NotFound /> } />
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App
