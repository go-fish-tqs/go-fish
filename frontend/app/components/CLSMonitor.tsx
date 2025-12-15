"use client";

import { useEffect } from "react";
import { initCLSMonitoring } from "@/app/lib/cls-monitor";

export default function CLSMonitor() {
  useEffect(() => {
    initCLSMonitoring();
  }, []);

  return null;
}
