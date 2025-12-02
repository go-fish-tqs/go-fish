import Image from "next/image";

export default function Home() {
  return (
    // **Main Page Container:**
    // Updated: `dark:bg-black` -> `dark:bg-slate-900` (A softer dark gray/blue that is less harsh than pure black)
    <div className="h-full flex items-center justify-center">

      {/* **Content Wrapper (The Card):** */}
      <main className="w-full max-w-4xl p-12 bg-white dark:bg-slate-800 shadow-2xl rounded-xl sm:p-16">

        {/* Logo & Header */}
        <div className="flex items-center gap-4 mb-12">
          <Image
            src="/gofish-logo.png"
            alt="GoFish Logo"
            width={60}
            height={60}
            className="rounded-lg shadow-md"
          />
          <h1 className="text-4xl font-extrabold tracking-tight text-blue-700 dark:text-blue-300">
            GoFish
          </h1>
        </div>

        {/* Hero section */}
        <div className="flex flex-col gap-6">
          <h2 className="max-w-xl text-5xl font-extrabold leading-tight tracking-tight text-gray-900 dark:text-gray-100">
            Book Fishing Gear Easily & <span className="text-blue-600 dark:text-blue-400">Quickly</span>
          </h2>

          {/* Updated: `dark:text-zinc-300` -> `dark:text-gray-300` (More standard light text color) */}
          <p className="max-w-2xl text-xl leading-relaxed text-zinc-600 dark:text-gray-300">
            Need a rod, bait, or a full kit? GoFish lets you browse available
            fishing equipment, reserve what you need, and pick it up when you’re
            ready. Simplify your trip planning now!
          </p>
        </div>

        {/* CTA buttons (No dark mode changes needed here as they use primary blue/white) */}
        <div className="flex flex-col gap-5 mt-12 text-lg font-semibold sm:flex-row">

          {/* Primary CTA */}
          <a
            href="/items"
            className="flex h-14 w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-8 text-white shadow-lg shadow-blue-500/50 transition-all duration-300 ease-in-out hover:bg-blue-700 hover:shadow-blue-500/70 sm:w-[240px]"
          >
            Browse Equipment
          </a>

          {/* Secondary CTA */}
          <a
            href="/dashboard"
            className="flex h-14 w-full items-center justify-center rounded-xl border-2 border-blue-600 px-8 text-blue-600 transition-colors duration-300 ease-in-out hover:bg-blue-50 hover:border-blue-700 sm:w-[240px]"
          >
            Go to Dashboard
          </a>
        </div>
      </main>
    </div>
  );
}