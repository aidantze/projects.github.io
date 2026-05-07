import { Button } from '@headlessui/react';

const ErrorPopup = ({ message, onClose }) => {
    if (!message) return null;

    return (
        <>
            <div className="fixed top-6 right-6 z-50 w-full max-w-sm rounded-xl bg-red-200 p-4 shadow-lg">
                <div className="flex items-start justify-between gap-4">
                    <p className="text-red-800 font-medium">{ message }</p>

                    <Button className="text-red-700 hover:text-red-900 hover:scale-120" onClick={ onClose }>
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" className="w-5 h-5 mb-1">
                            <path d="M5.00098 5L19 18.9991" stroke="#b01e1e" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                            <path d="M5.00009 18.9991L18.9991 5" stroke="#b01e1e" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                    </Button>
                </div>
            </div>
        </>
    );
}

export default ErrorPopup;