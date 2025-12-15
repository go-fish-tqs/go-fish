/**
 * CLS (Cumulative Layout Shift) Monitoring Utility
 * Helps identify and debug layout shift issues
 */

export function initCLSMonitoring() {
  if (typeof window === "undefined" || !("PerformanceObserver" in window)) {
    return;
  }

  try {
    const observer = new PerformanceObserver((entryList) => {
      for (const entry of entryList.getEntries()) {
        // Ignore shifts caused by user input
        if (!(entry as any).hadRecentInput) {
          const layoutShiftEntry = entry as any;
          console.warn(
            `[CLS] Layout shift detected: ${layoutShiftEntry.value.toFixed(3)}`,
            {
              sources: layoutShiftEntry.sources?.map((source: any) => ({
                node: source.node,
                previousRect: source.previousRect,
                currentRect: source.currentRect,
              })),
            }
          );
        }
      }
    });

    observer.observe({ type: "layout-shift", buffered: true });
  } catch (e) {
    console.debug("CLS monitoring not available:", e);
  }
}

/**
 * Reserve space for content that may load later
 * This is a helper component for preventing layout shifts
 */
export const CLS_PREVENTION = {
  // Images should have explicit width/height or aspect-ratio
  IMAGE_STYLES: "w-full h-auto",

  // Containers should have min-height or explicit height
  CONTAINER_STYLES: "min-h-[200px]",

  // Loading states should match content dimensions
  SKELETON_STYLES: "bg-gray-200 animate-pulse",
};
