import { useState } from "react";
import { FaHome, FaUser, FaSignInAlt, FaBars, FaTimes } from "react-icons/fa";
import { Link } from "react-router-dom";

const Navbar = () => {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <nav className="fixed top-0 left-0 w-full bg-blue-600 text-white shadow-md z-50">
      <div className="container mx-auto flex justify-between items-center p-4">
        <h1 className="text-2xl font-bold">
          <Link to="/">SpringMERNify</Link>
        </h1>

        {/* Desktop Menu */}
        <div className="hidden md:flex gap-8">
          <Link to="/" className="flex items-center gap-2 hover:text-gray-300">
            <FaHome /> Home
          </Link>
          <Link
            to="/profile"
            className="flex items-center gap-2 hover:text-gray-300"
          >
            <FaUser /> Profile
          </Link>
          <Link
            to="/signin"
            className="flex items-center gap-2 hover:text-gray-300"
          >
            <FaSignInAlt /> Sign In
          </Link>
        </div>

        {/* Mobile Menu Button */}
        <button
          className="md:hidden text-2xl"
          onClick={() => setMenuOpen(!menuOpen)}
        >
          {menuOpen ? <FaTimes /> : <FaBars />}
        </button>
      </div>

      {/* Mobile Menu */}
      {menuOpen && (
        <div className="md:hidden bg-blue-700">
          <Link
            to="/"
            className="block px-4 py-2 hover:bg-blue-800"
            onClick={() => setMenuOpen(false)}
          >
            <FaHome className="inline mr-2" /> Home
          </Link>
          <Link
            to="/profile"
            className="block px-4 py-2 hover:bg-blue-800"
            onClick={() => setMenuOpen(false)}
          >
            <FaUser className="inline mr-2" /> Profile
          </Link>
          <Link
            to="/signin"
            className="block px-4 py-2 hover:bg-blue-800"
            onClick={() => setMenuOpen(false)}
          >
            <FaSignInAlt className="inline mr-2" /> Sign In
          </Link>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
