# Featured Projects Section — Web Building Prompt

Create the third major portfolio section of the personal software engineer portfolio.

Section identifier:

03 / PORTFOLIO

Section title:

Featured Projects

Section subtitle:

Production systems, developer tools, and scalable platforms.

The visual language MUST remain consistent with the previous Hero, About, and Technical Expertise sections.

---

## 1. Section Layout

Use the same centered content container as the previous sections.

Desktop:

- Full-width section within the main content container
- Generous top and bottom whitespace
- Section heading aligned left
- Category filter aligned toward the upper-right
- Project cards displayed in a 2-column grid

Mobile:

- Section heading remains left aligned
- Filter becomes horizontally scrollable or wraps
- Project cards become one column

---

# 2. Section Header

Create a small technical section label:

03 / PORTFOLIO

Place a short horizontal purple line immediately before the label.

Style:

- Monospace font
- Uppercase
- Purple
- Very small font
- Increased letter spacing

Below it:

## Featured Projects

Use:

- Heavy black typography
- Approximately 29–31px desktop
- Tight line-height

Subtitle:

Production systems, developer tools, and scalable platforms.

Use:

- 13–14px
- Dark muted blue-gray
- Regular weight

---

# 3. Project Filter

Position the filter navigation on the right side of the section heading area.

Create a rounded pill container.

Filter options:

- All
- Cloud & Infra
- Full-Stack
- DevTools

The "All" option is active.

Active:

- Purple filled rounded pill
- White text

Inactive:

- Transparent/light background
- Dark purple/gray text

Container:

- White or warm-white background
- Thin beige border
- Fully rounded
- Compact height

Add interactive filtering behavior.

When a filter is selected:

- Update active state
- Show matching projects
- Use a subtle transition
- Do not cause layout jumping

---

# 4. Project Grid

Desktop:

2 columns.

Gap:

approximately 18–20px.

Each project card should have approximately equal height.

Use:

- White background
- Very subtle warm beige border
- 15–17px border radius
- Generous internal padding

Cards should feel like premium SaaS/product cards.

Avoid excessive shadows.

---

# 5. Project Card Structure

Every card follows this structure:

TOP ROW

Left:
project category badge

Right:
project status / metric

MIDDLE

Project name

Project description

Technology tags

BOTTOM

Horizontal divider

Project links

---

# 6. Category Badge

Create a small rectangular rounded badge.

Examples:

CLOUD / INFRA

DATA / INFRA

DESIGN SYSTEM

DEVELOPER TOOLS

Style:

- Very light warm beige background
- Thin subtle border
- Purple monospace uppercase text
- Tiny font
- Small horizontal padding
- Rounded approximately 5px

---

# 7. Status Indicator

Place a small colored status dot followed by status text.

Examples:

● Live V3

● 100k+ evt/sec

● 15+ Teams

● 8.4k Installs

Status colors:

Live:
green

Performance metric:
orange

Teams:
purple

Installs:
green

Keep the indicator very small and subtle.

---

# 8. Project 01

Category:

CLOUD / INFRA

Status:

Live V3

Project title:

NexusCloud Orchestrator

Description:

Multi-region container orchestration platform with automated failure recovery routines and real-time telemetry streaming at sub-5ms latency.

Technology tags:

Go
Kubernetes
React
gRPC

Bottom links:

Live Demo
Source Repository

Use external-link icon for Live Demo.

Use code/repository icon for Source Repository.

---

# 9. Project 02

Category:

DATA / INFRA

Status:

100k+ evt/sec

Project title:

PulseStream Analytics

Description:

Real-time high-throughput streaming engine processing over 100k events/sec. Features fast analytical rollup queries and unified dashboard rendering.

Technology tags:

Rust
Apache Kafka
Next.js
ClickHouse

Bottom links:

Live Demo
Source Repository

---

# 10. Project 03

Category:

DESIGN SYSTEM

Status:

15+ Teams

Project title:

Aura Design System

Description:

Accessible, design-token-driven component library with full WAI-ARIA compliance. Adopted widely to prevent visual drift and accelerate velocity.

Technology tags:

TypeScript
Tailwind CSS
Storybook
Radix UI

Bottom links:

Storybook
GitHub Package

---

# 11. Project 04

Category:

DEVELOPER TOOLS

Status:

8.4k Installs

Project title:

DevSync CLI & Hub

Description:

Zero-config local environment synchronization tool. Automates secrets distribution and docker container orchestration across distributed engineers.

Technology tags:

Python
FastAPI
SQLite
Electron

Bottom links:

Download v2.4
Source Repository

---

# 12. Project Card Typography

Project title:

- Approximately 15–16px
- Bold
- Black

Description:

- Approximately 10–11px
- Slate/dark blue-gray
- Line height around 1.5

Technology tags:

- 9px approximately
- Monospace or technical font
- Dark text
- Warm white background
- Beige border

Links:

- 10px approximately
- Purple for primary action
- Muted gray for repository action

---

# 13. Card Spacing

Card padding:

approximately 25–26px.

Category/status row:
top aligned.

Project title:
approximately 17–20px below the top metadata.

Description:
approximately 8px below title.

Technology tags:
approximately 17px below description.

Divider:
approximately 20px below tags.

Bottom links:
approximately 15px below divider.

Keep the vertical rhythm consistent across all four cards.

---

# 14. Responsive Behavior

Desktop:
2-column grid.

Tablet:
2-column grid if enough width.

Mobile:
1-column grid.

Project filter:

Desktop:
right aligned.

Mobile:
full width below subtitle.

The filter may become horizontally scrollable if the viewport is too narrow.

Cards must never become horizontally clipped.

---

# 15. Interaction

Project cards:

- Slight border color transition on hover
- Very subtle elevation
- No exaggerated movement

Technology tags:

- Subtle hover background transition

Links:

- Purple
- Underline or color transition on hover

Filter:

- Smooth active-state transition

Do not add excessive animations.