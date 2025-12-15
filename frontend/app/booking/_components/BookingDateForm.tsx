import Link from "next/link";

interface BookingDateFormProps {
  startDate: string;
  endDate: string;
  onStartDateChange: (date: string) => void;
  onEndDateChange: (date: string) => void;
  onSubmit: (e: React.FormEvent) => void;
  isSubmitting: boolean;
}

export default function BookingDateForm({
  startDate,
  endDate,
  onStartDateChange,
  onEndDateChange,
  onSubmit,
  isSubmitting,
}: BookingDateFormProps) {
  const today = new Date().toISOString().split("T")[0];

  // Calculate duration and total
  const calculateDays = () => {
    if (!startDate || !endDate) return 0;
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diff = Math.ceil(
      (end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)
    );
    return diff > 0 ? diff : 0;
  };

  const days = calculateDays();

  return (
    <form onSubmit={onSubmit} className="flex-1 flex flex-col">
      {/* Glass Card */}
      <div className="backdrop-blur-xl bg-white/60 border border-white/40 rounded-2xl p-6 shadow-xl flex-1 flex flex-col">
        <h2 className="text-lg font-semibold text-gray-800 mb-6">
          Select Dates
        </h2>

        <div className="space-y-5 flex-1">
          <div>
            <label
              htmlFor="startDate"
              className="block text-sm font-medium text-gray-600 mb-2"
            >
              Start Date
            </label>
            <input
              type="date"
              id="startDate"
              value={startDate}
              onChange={(e) => onStartDateChange(e.target.value)}
              min={today}
              required
              className="w-full px-4 py-3.5 rounded-xl bg-white/80 backdrop-blur border border-gray-200/50 focus:ring-2 focus:ring-blue-400/50 focus:border-blue-400 focus:outline-none transition-all shadow-sm"
            />
          </div>

          <div>
            <label
              htmlFor="endDate"
              className="block text-sm font-medium text-gray-600 mb-2"
            >
              End Date
            </label>
            <input
              type="date"
              id="endDate"
              value={endDate}
              onChange={(e) => onEndDateChange(e.target.value)}
              min={startDate || today}
              required
              className="w-full px-4 py-3.5 rounded-xl bg-white/80 backdrop-blur border border-gray-200/50 focus:ring-2 focus:ring-blue-400/50 focus:border-blue-400 focus:outline-none transition-all shadow-sm"
            />
          </div>

          {/* Duration Display */}
          {days > 0 && (
            <div className="mt-2 p-4 rounded-xl bg-gradient-to-r from-blue-50/80 to-indigo-50/80 border border-blue-100/50">
              <div className="flex justify-between items-center">
                <span className="text-gray-600">Duration</span>
                <span className="font-semibold text-gray-800">
                  {days} day{days !== 1 ? "s" : ""}
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col gap-3 mt-6 pt-6 border-t border-gray-200/50">
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-gradient-to-r from-blue-500 via-blue-600 to-indigo-600 hover:from-blue-600 hover:via-blue-700 hover:to-indigo-700 text-white font-semibold py-4 px-6 rounded-xl transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-blue-500/25 hover:shadow-xl hover:shadow-blue-500/30 hover:-translate-y-0.5 active:translate-y-0"
          >
            {isSubmitting ? (
              <span className="flex items-center justify-center gap-2">
                <svg
                  className="animate-spin h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Creating Booking...
              </span>
            ) : (
              "Create Booking"
            )}
          </button>
          <Link
            href="/items"
            className="w-full px-6 py-3.5 backdrop-blur bg-white/60 border border-gray-200/50 text-gray-700 font-medium rounded-xl hover:bg-white/80 transition-all text-center shadow-sm"
          >
            Cancel
          </Link>
        </div>
      </div>
    </form>
  );
}
