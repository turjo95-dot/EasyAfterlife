import React from 'react';
import { Header } from './components/Header';
import { Hero } from './components/Hero';
import { Features } from './components/Features';
import { Commands } from './components/Commands';
import { Configuration } from './components/Configuration';
import { Download } from './components/Download';
import { Footer } from './components/Footer';

function App() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <Header />
      <Hero />
      <Features />
      <Commands />
      <Configuration />
      <Download />
      <Footer />
    </div>
  );
}

export default App;