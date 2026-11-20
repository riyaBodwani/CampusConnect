import './Logo.css'

export default function Logo({
  size = 'md',
  showTagline = false,
  showWordmark = true,
  className = '',
}) {
  // Option 3 exact proportions: viewBox="0 0 216 198" (width: 216, height: 198)
  const sizePresets = {
    sm: { width: 44, height: 40 },
    md: { width: 52, height: 48 },
    lg: { width: 68, height: 62 },
  }

  const dimensions =
    typeof size === 'number'
      ? { width: size, height: Math.round(size * (198 / 216)) }
      : sizePresets[size] || sizePresets.md

  return (
    <div
      className={`brand-logo brand-logo--${typeof size === 'string' ? size : 'custom'} ${
        showTagline ? 'brand-logo--with-tagline' : ''
      } ${className}`}
    >
      <svg
        width={dimensions.width}
        height={dimensions.height}
        viewBox="0 0 216 198"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="brand-logo-icon"
        aria-hidden="true"
      >
        {/* Bold purple C shape with 45° angled parallel terminal cuts */}
        <path
          d="M 166 30 A 96 96 0 1 0 166 166 L 138 138 A 56 56 0 1 1 138 58 Z"
          fill="#8b5cf6"
        />

        {/* Option 3 Teal Paper Plane / Arrow in the opening of the C */}
        {/* Top Wing */}
        <polygon
          points="210,58 99,90 138,111"
          fill="#02c3aa"
        />

        {/* Keel / Underside fold */}
        <polygon
          points="138,111 137,128 150,117"
          fill="#02c3aa"
        />

        {/* Bottom Wing */}
        <polygon
          points="210,58 150,117 171,144"
          fill="#02c3aa"
        />

        {/* Clean white negative-space crease lines */}
        <line
          x1="138"
          y1="111"
          x2="210"
          y2="58"
          stroke="#ffffff"
          strokeWidth="3.5"
          strokeLinecap="round"
        />
        <line
          x1="138"
          y1="111"
          x2="150"
          y2="117"
          stroke="#ffffff"
          strokeWidth="2.5"
          strokeLinecap="round"
        />
      </svg>

      {showWordmark && (
        <div className="brand-wordmark-container">
          <span className="brand-wordmark">
            <span className="wordmark-campus">Campus</span>
            <span className="wordmark-connect">Connect</span>
          </span>
          {showTagline && (
            <span className="brand-tagline">Bridging campus and careers</span>
          )}
        </div>
      )}
    </div>
  )
}
