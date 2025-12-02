"use client";

import Link from "next/link";
import Image from "next/image";

export default function Sidebar() {
  return (
    <aside className="h-screen w-fit pr-18 bg-white dark:bg-blue-950 border-r border-gray-200 dark:border-zinc-800 flex flex-col p-5">
      {/* LOGO */}
      <div className="flex items-center gap-3 mb-10">
        {/* <Image
          src="/gofish-logo.png"
          alt="GoFish Logo"
          width={40}
          height={40}
        /> */}
        <h1 className="text-2xl font-semibold text-blue-600 dark:text-blue-200">
          GoFish
        </h1>
      </div>

      {/* NAV LINKS */}
      <nav className="flex flex-col gap-3 text-gray-700 dark:text-gray-300">
        <Link href="/" className="hover:text-blue-600 dark:hover:text-blue-400">
          Home
        </Link>

        <Link
          href="/dashboard"
          className="hover:text-blue-600 dark:hover:text-blue-400"
        >
          Dashboard
        </Link>

        <Link
          href="/items"
          className="hover:text-blue-600 dark:hover:text-blue-400"
        >
          Browse Items
        </Link>
        <Link
          href="/items/add"
          className="hover:text-blue-600 dark:hover:text-blue-400"
        >
          Add Item
        </Link>
      </nav>
    </aside>
  );
}
