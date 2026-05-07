import { useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import { Button } from '@headlessui/react'

import Navbar from '../components/Navbar';

const NotFound = () => {
  useEffect(() => {
    document.title = "404 Not Found";
  }, []);

  const navigate = useNavigate();
  //   localStorage.setItem('token', "sweighowefg");
  //   const token = localStorage.getItem('token');

  const handleRedirect = () => {
    navigate("/", { replace: true });
  }

  return (
    <>
      <Navbar />
      <div className="mx-10">
        <h1 className="my-8 text-presto-accent font-bold text-4xl">404 Page Not Found</h1>
        <p>{ "Hmm. Looks like this slide didn't make the final cut. We couldn't find the page you're looking for, but don't let it ruin your flow. Double-check your URL or use the button below to get back to the show." }</p>
        <Button className="btn-presto btn-primary text-lg px-8 py-3 rounded-xl my-8" onClick={ handleRedirect }>
          Back to Safety
        </Button>
      </div>
    </>
  );
}

export default NotFound;