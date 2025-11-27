"use client";

import React from "react";

export default function ProfileCard() {
  return (
    <div className="flex items-center gap-3 px-3 py-1 border rounded-lg bg-white shadow-sm cursor-pointer hover:bg-gray-50">
      <img
        src="https://ui-avatars.com/api/?name=John+Doe"
        alt="Profile"
        className="w-8 h-8 rounded-full"
      />
      <div className="flex flex-col">
        <span className="font-medium text-sm">John Doe</span>
        <span className="text-xs text-gray-500">View Profile</span>
      </div>
    </div>
  );
}
