import React from 'react';
import Sidebar from './Sidebar';
import Topbar from './Topbar';

const Layout = ({ children }) => {
  return (
    <div id="wrapper">
      <Sidebar />
      <div id="content-wrapper">
        <Topbar />
        <div className="container-fluid">
          {children}
        </div>
        <footer className="sticky-footer">
          <div className="container text-center">
            <span>Copyright &copy; Football Manager Admin 2026</span>
          </div>
        </footer>
      </div>
    </div>
  );
};

export default Layout;
