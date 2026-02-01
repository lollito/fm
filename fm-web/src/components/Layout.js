import React, { useState } from 'react';
import Navbar from './Navbar';
import Sidebar from './Sidebar';

const Layout = ({ children }) => {
  const [menuToggled, setMenuToggled] = useState(false);

  const toggleMenu = () => {
    setMenuToggled(!menuToggled);
  };

  return (
    <div className={menuToggled ? 'toggled' : ''} id="wrapper">
      <Sidebar />
      <div id="page-content-wrapper">
        <Navbar onToggleMenu={toggleMenu} />
        <div className="container-fluid">
          {children}
        </div>
      </div>
    </div>
  );
};

export default Layout;
