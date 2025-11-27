import Image from "next/image";

export default function Home() {
  return (
    <div className="flex items-center justify-center bg-blue-50 font-sans dark:bg-black h-full">
      <main className="max-h-fit h-full w-full max-w-3xl flex-col items-center justify-center py-16 px-12 bg-white dark:bg-black sm:items-start">
        {/* Logo */}
        <div className="flex items-center gap-3 mb-10">
          <Image
            src="/gofish-logo.png"
            alt="GoFish Logo"
            width={70}
            height={70}
            className="rounded-md"
          />
          <h1 className="text-4xl font-bold tracking-tight text-blue-700 dark:text-blue-300">
            GoFish
          </h1>
        </div>

        {/* Hero section */}
        <div className="flex flex-col items-center gap-6 text-center sm:items-start sm:text-left">
          <h2 className="max-w-md text-3xl font-semibold leading-10 tracking-tight text-gray-900 dark:text-zinc-50">
            Book Fishing Gear Easily & Quickly
          </h2>

          <p className="max-w-md text-lg leading-8 text-zinc-600 dark:text-zinc-400">
            Need a rod, bait, or a full kit? GoFish lets you browse available
            fishing equipment, reserve what you need, and pick it up when you’re
            ready.
          </p>
        </div>

        {/* CTA buttons */}
        <div className="flex flex-col gap-4 mt-10 text-base font-medium sm:flex-row">
          <a
            href="/items"
            className="flex h-12 w-full items-center justify-center gap-2 rounded-full bg-blue-600 px-5 text-white transition-colors hover:bg-blue-700 md:w-[180px]"
          >
            Browse Equipment
          </a>

          <a
            href="/dashboard"
            className="flex h-12 w-full items-center justify-center rounded-full border border-blue-600 px-5 text-blue-600 transition-colors hover:bg-blue-100 md:w-[180px]"
          >
            Go to Dashboard
          </a>
        </div>
      </main>
    </div>
  );
}
