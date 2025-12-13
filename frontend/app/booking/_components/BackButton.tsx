import Link from "next/link";

interface BackButtonProps {
    href: string;
    label?: string;
}

export default function BackButton({ href, label = "Back" }: BackButtonProps) {
    return (
        <Link
            href={href}
            className="inline-flex items-center gap-2 text-sm font-medium text-gray-600 hover:text-blue-600 mb-8 transition-colors group"
        >
            <span className="w-8 h-8 rounded-full backdrop-blur bg-white/60 border border-white/40 flex items-center justify-center shadow-sm group-hover:bg-white/80 group-hover:shadow-md transition-all">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-4 w-4"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth={2}
                >
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
                </svg>
            </span>
            {label}
        </Link>
    );
}
