import Link from "next/link";

export default function Home() {
  return (
    <div className="relative h-full w-full overflow-hidden rounded-4xl">
      {/* Animated Background */}
      <div className="absolute inset-0 bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-slate-900 dark:via-slate-900 dark:to-indigo-950" />

      {/* Animated Gradient Orbs */}
      <div className="absolute top-1/4 -left-20 w-96 h-96 bg-blue-400/30 rounded-full blur-3xl animate-pulse" />
      <div className="absolute bottom-1/4 -right-20 w-96 h-96 bg-indigo-400/30 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-gradient-radial from-blue-200/20 via-indigo-200/10 to-transparent rounded-full blur-3xl" />

      {/* Edge Gradient Masks - smooth transitions on borders */}
      <div className="absolute inset-x-0 top-0 h-32 bg-gradient-to-b from-slate-100/80 via-slate-50/40 to-transparent dark:from-slate-950/80 dark:via-slate-900/40 pointer-events-none" />
      <div className="absolute inset-x-0 bottom-0 h-32 bg-gradient-to-t from-indigo-100/80 via-blue-50/40 to-transparent dark:from-indigo-950/80 dark:via-slate-900/40 pointer-events-none" />
      <div className="absolute inset-y-0 left-0 w-32 bg-gradient-to-r from-slate-100/60 via-slate-50/30 to-transparent dark:from-slate-950/60 dark:via-slate-900/30 pointer-events-none" />
      <div className="absolute inset-y-0 right-0 w-32 bg-gradient-to-l from-indigo-100/60 via-blue-50/30 to-transparent dark:from-indigo-950/60 dark:via-slate-900/30 pointer-events-none" />

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col items-center justify-center px-6">
        {/* Logo Badge - blended */}
        <div className="inline-flex items-center gap-3 px-5 py-2.5 mb-8 rounded-full bg-gradient-to-r from-blue-100/40 to-indigo-100/40 dark:from-blue-900/30 dark:to-indigo-900/30 backdrop-blur-sm">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500/80 to-indigo-600/80 flex items-center justify-center">
            <svg className="w-5 h-5 text-white/90" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12a8 8 0 01-8 8m8-8a8 8 0 00-8-8m8 8h-8m0 8a8 8 0 01-8-8m8 8v-8m-8 0a8 8 0 018-8m-8 8h8m0-8v8" />
            </svg>
          </div>
          <span className="text-sm font-semibold text-blue-700/80 dark:text-blue-300/80">
            GoFish
          </span>
        </div>

        {/* Main Headline - softer colors */}
        <h1 className="text-5xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight mb-6 text-center">
          <span className="text-gray-800/90 dark:text-white/90">Book Fishing Gear</span>
          <br />
          <span className="bg-gradient-to-r from-blue-500/90 via-indigo-500/90 to-purple-500/90 bg-clip-text text-transparent">
            Easily & Quickly
          </span>
        </h1>

        {/* Subtitle */}
        <p className="max-w-lg text-center text-lg text-gray-600/80 dark:text-gray-300/80 leading-relaxed mb-10">
          Browse equipment, reserve what you need, and pick it up when you&apos;re ready.
        </p>

        {/* CTA Buttons - blended */}
        <div className="flex flex-col sm:flex-row items-center gap-4">
          <Link
            href="/items"
            className="group flex h-14 items-center justify-center gap-3 rounded-2xl bg-gradient-to-r from-blue-500/90 to-indigo-500/90 px-8 text-white/95 font-semibold shadow-lg shadow-blue-500/20 transition-all duration-300 hover:from-blue-600 hover:to-indigo-600 hover:shadow-xl hover:shadow-blue-500/30"
          >
            Browse Equipment
            <svg className="w-4 h-4 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </Link>

          <Link
            href="/dashboard"
            className="flex h-14 items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-white/30 to-blue-50/30 dark:from-slate-800/30 dark:to-indigo-900/30 backdrop-blur-sm px-8 text-gray-700/90 dark:text-gray-200/90 font-semibold transition-all duration-300 hover:from-white/50 hover:to-blue-100/50 dark:hover:from-slate-800/50 dark:hover:to-indigo-900/50"
          >
            Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}