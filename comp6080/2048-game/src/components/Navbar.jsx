import { useState } from 'react';
import { useNavigate } from "react-router-dom";

import ErrorPopup from './ErrorPopup';

const Navbar = () => {
  const navigate = useNavigate();
  const [error, setError] = useState("");

  return (
    <>
      <ErrorPopup message={ error } onClose={ () => setError("") } />
      <nav className="w-full bg-primary border-b-4 border-black px-8 py-4 sticky top-0 z-50">
        <div className="w-full flex justify-between items-center">
          <div
            className="flex items-center cursor-pointer"
            onClick={ () => navigate('/') }
          >
            <img
              src="/logo.png"
              alt="exam-final Logo"
              className="w-10 h-10 object-contain rounded-full"
            />
            <span className="ml-3 text-2xl font-bold tracking-wider text-black">
              comp 6080
            </span>
          </div>
        </div>
      </nav>
    </>
  );
}

export default Navbar;