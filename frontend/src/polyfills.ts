/**
 * This file includes polyfills needed by Angular and is loaded before the app.
 * You can add your own extra polyfills to this file.
 */

import 'zone.js';  // Included with Angular CLI.

/***************************************************************************************************
 * BROWSER POLYFILLS FOR NODE.JS GLOBALS
 * Required for libraries like @stomp/stompjs and sockjs-client
 */

// Polyfill for 'global' (used by Node.js libraries in browser)
(window as any).global = window;

// Polyfill for 'process' (used by some Node.js libraries)
(window as any).process = {
  env: { DEBUG: undefined },
  version: '',
  browser: true
};

// Polyfill for 'Buffer' if needed by some libraries
if (typeof (window as any).Buffer === 'undefined') {
  (window as any).Buffer = {
    isBuffer: () => false
  };
}
