export default function Carrot({ size = 28 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 36 36"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      {/* Leaves */}
      <path d="M18 10 C16 6, 11 4, 10 1 C12 3, 15 4, 17 7" fill="#4a7c59" />
      <path d="M18 10 C19 5, 23 3, 25 0 C23 3, 20 5, 18 8" fill="#5d9970" />
      <path d="M18 10 C17 6, 14 2, 13 0 C15 2, 17 5, 18 9" fill="#3d6b4a" />
      {/* Body */}
      <path
        d="M14 10 Q12 18 15 28 Q17 34 18 35 Q19 34 21 28 Q24 18 22 10 Z"
        fill="#E8732A"
      />
      {/* Highlight */}
      <path
        d="M16 11 Q14.5 18 16.5 26"
        stroke="#F5A05A"
        strokeWidth="1.5"
        strokeLinecap="round"
        opacity="0.6"
      />
    </svg>
  );
}
