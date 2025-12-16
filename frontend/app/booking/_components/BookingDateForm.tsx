import Link from "next/link";
import toast from "react-hot-toast";

interface BookingDateFormProps {
  startDate: string;
  endDate: string;
  onStartDateChange: (date: string) => void;
  onEndDateChange: (date: string) => void;
  onSubmit: (e: React.FormEvent) => void;
  isSubmitting: boolean;
  unavailableDates?: string[];
}

export default function BookingDateForm({
  startDate,
  endDate,
  onStartDateChange,
  onEndDateChange,
  onSubmit,
  isSubmitting,
  unavailableDates = [],
}: BookingDateFormProps) {
  const today = new Date().toISOString().split("T")[0];

  // Helper function to check if a date conflicts with unavailable dates
  const isDateUnavailable = (dateStr: string): boolean => {
    if (!dateStr || unavailableDates.length === 0) return false;

    const date = new Date(dateStr);
    return unavailableDates.some(blockedDate => {
      const blocked = new Date(blockedDate);
      return date.toDateString() === blocked.toDateString();
    });
  };

  // Check if date range overlaps with blocked dates
  const doDateRangesOverlap = (start: string, end: string): boolean => {
    if (!start || !end || unavailableDates.length === 0) return false;

    const startDate = new Date(start);
    const endDate = new Date(end);

    return unavailableDates.some(blockedDateStr => {
      const blockedDate = new Date(blockedDateStr);
      return blockedDate >= startDate && blockedDate <= endDate;
    });
  };

  // Handle start date change with validation
  const handleStartDateChange = (date: string) => {
    if (isDateUnavailable(date)) {
      toast.error("This date is already booked. Please select a different date.");
      return;
    }

    // Check if new range would overlap with blocked dates
    if (endDate && doDateRangesOverlap(date, endDate)) {
      toast.error("Selected date range includes dates that are already booked.");
    }

    onStartDateChange(date);
  };

  // Handle end date change with validation
  const handleEndDateChange = (date: string) => {
    if (isDateUnavailable(date)) {
      toast.error("This date is already booked. Please select a different date.");
      return;
    }

    // Check if new range would overlap with blocked dates
    if (startDate && doDateRangesOverlap(startDate, date)) {
      toast.error("Selected date range includes dates that are already booked.");
    }

    onEndDateChange(date);
  };

  // Handle form submission with date conflict validation
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Check for date conflicts before submitting
    if (startDate && endDate && doDateRangesOverlap(startDate, endDate)) {
      toast.error("Selected dates conflict with existing bookings. Please choose different dates.");
      return;
    }

    onSubmit(e);
  };

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

  // Check if current selection has conflicts
  const hasConflict = startDate && endDate && doDateRangesOverlap(startDate, endDate);

  return (
    <form onSubmit={handleSubmit} className="flex-1 flex flex-col">
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
              onChange={(e) => handleStartDateChange(e.target.value)}
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
              onChange={(e) => handleEndDateChange(e.target.value)}
              min={startDate || today}
              required
              className="w-full px-4 py-3.5 rounded-xl bg-white/80 backdrop-blur border border-gray-200/50 focus:ring-2 focus:ring-blue-400/50 focus:border-blue-400 focus:outline-none transition-all shadow-sm"
            />
          </div>

          {/* Duration Display */}
          {days > 0 && (
            <div className={`mt-2 p-4 rounded-xl border ${hasConflict
              ? 'bg-gradient-to-r from-red-50/80 to-orange-50/80 border-red-100/50'
              : 'bg-gradient-to-r from-blue-50/80 to-indigo-50/80 border-blue-100/50'}`}>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">Duration</span>
                <span className="font-semibold text-gray-800">
                  {days} day{days !== 1 ? "s" : ""}
                </span>
              </div>
              {hasConflict && (
                <p className="text-red-600 text-sm mt-2">
                  ⚠️ Selected dates conflict with existing bookings
                </p>
              )}
            </div>
          )}
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col gap-3 mt-6 pt-6 border-t border-gray-200/50">
          <button
            type="submit"
            disabled={isSubmitting || hasConflict}
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
            ) : hasConflict ? (
              "Cannot Book - Dates Unavailable"
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
