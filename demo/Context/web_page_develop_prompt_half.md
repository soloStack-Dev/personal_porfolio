# Portfolio Website — Web Building Prompt

## Role

You are an expert frontend developer and UI engineer. Recreate the provided portfolio website screenshots as a production-quality, responsive personal portfolio website.

The screenshots represent three consecutive sections of the same website:

1. Hero / Home
2. About My Work / Foundation
3. Technical Expertise / Capabilities

The implementation must prioritize visual accuracy, clean component architecture, responsive behavior, accessibility, and maintainability.

---

# 1. Overall Website Direction

Create a premium modern software engineer portfolio website with a minimalist editorial/SaaS aesthetic.

Visual characteristics:

- White / warm off-white background
- Deep black typography
- Purple as the primary accent
- Purple-to-coral gradient for primary CTAs and highlighted text
- Very subtle beige borders
- Rounded cards
- Generous whitespace
- Clean professional typography
- Small uppercase technical labels
- Thin dividers
- Minimal line icons
- No excessive decoration
- Premium enterprise software feeling
- Strong typography hierarchy
- Editorial-style section numbering

The website should feel like a senior software engineer / systems architect portfolio rather than a generic developer template.

---

# 2. Global Page Structure

Create the following structure:

<App>

  <Header />

  <main>

    <HeroSection />

    <AboutSection />

    <ExpertiseSection />

  </main>

</App>

Prepare the architecture so additional sections such as Projects and Contact can be added later without restructuring the existing layout.

---

# 3. Global Container

Use a centered content container.

Desktop:

- Maximum content width approximately 845–850px based on the screenshots
- Horizontally centered
- Comfortable left/right whitespace
- Sections should align to the same content grid

The hero image may extend slightly beyond the main text width while remaining aligned with the overall container.

On large screens, do not stretch the content across the entire viewport.

On smaller screens:

- Container width: calc(100% - 32px)
- Maintain approximately 16px horizontal padding
- Stack multi-column layouts vertically

---

# 4. Header

Create a clean horizontal navigation header.

Header layout:

LEFT:
- Small rounded-square logo containing "AR"
- Purple gradient background
- White letters
- Name: "Alex Rivera"
- Subtitle: "Systems & Full-Stack"

CENTER:
- Rounded pill navigation container
- Items:
  - Home
  - About
  - Skills
  - Projects
  - Contact
- Active item: Home
- Active navigation item has a purple rounded pill background and white text

RIGHT:
- "Get in touch" CTA button
- Purple-to-coral gradient
- White text
- Right arrow icon

Header characteristics:

- White background
- Thin subtle bottom border
- Height approximately 64px
- Logo and text vertically centered
- Navigation centered
- CTA aligned to the right

Mobile behavior:

- Keep logo/name visible
- Hide desktop navigation
- Replace navigation with a compact mobile menu button
- Keep CTA accessible or convert it into an icon/button depending on screen width

---

# 5. Hero Section

Create a two-column hero layout.

LEFT COLUMN:

Top availability badge:

"Available for select advisory & senior projects"

Style:
- Small rounded pill
- Warm off-white background
- Thin beige border
- Small purple status dot
- Small dark text

Below the badge:

Small uppercase eyebrow:

"SENIOR FULL-STACK & SYSTEMS ARCHITECT"

Use:
- Purple text
- Monospace or technical font
- Letter spacing
- Small font size
- Uppercase

Main headline:

"Crafting scalable
architecture & fluid
digital products."

The words:

"Crafting scalable"
"architecture &"

should use dark/black text.

The highlighted phrase:

"fluid
digital products."

should use a purple-to-coral gradient.

Headline characteristics:

- Very large
- Heavy/bold
- Tight line-height
- Editorial typography
- Strong visual hierarchy
- Left aligned

Below the headline:

Paragraph:

"Over 7+ years delivering resilient cloud backends, high-throughput microservices, and human-centric frontends. Bridging rigorous engineering with fine aesthetic craft."

Use:
- Medium/small body text
- Comfortable line-height
- Dark blue-gray text
- Maximum width around 470px

CTA row:

Primary:
"Explore Work →"

- Purple-to-coral gradient
- White text
- Rounded pill
- Soft shadow

Secondary:
"Let's Talk"

- White background
- Beige border
- Dark text
- Rounded pill

Third action:
Document icon + "Resume"

- No filled background
- Simple text/icon treatment

Below CTA row:

Horizontal divider.

Then:

"CONNECT"

followed by three circular icon buttons:

- Code / GitHub style icon
- Briefcase / professional icon
- Mail icon

Each icon button should have:
- Warm off-white background
- Circular shape
- Minimal dark/purple icon

---

# 6. Hero Image Card

RIGHT COLUMN:

Create a premium image card.

Outer card:

- White background
- Thin beige border
- Rounded corners
- Subtle shadow
- Padding around image

Inside:

Use the provided/generated professional female software engineer photograph.

Image:
- Landscape/portrait crop depending on implementation
- Rounded corners
- Professional office environment
- Person centered/right
- Natural lighting

At the bottom of the photograph place a floating status panel.

Status panel:

Left:
- Green status dot
- "San Francisco, CA / Remote"

Right:
- "99.99% Uptime"
- Purple text

Panel characteristics:

- White / slightly translucent background
- Rounded corners
- Thin border
- Small shadow
- Compact height

Important:
The status panel is UI and must NOT be baked into the image.

---

# 7. About Section

Create a separate section with a warm off-white background.

Top section label:

"01 / FOUNDATION"

Precede it with a short horizontal purple line.

Heading:

"About My Work"

Subtitle:

"Engineering philosophy, background, and what drives long-term software quality."

Use the same typography system as the hero.

---

# 8. About Content Grid

Create a two-column desktop layout.

LEFT:
Large philosophy card.

RIGHT:
2 × 2 statistics grid.

---

# 9. Philosophy Card

Large rounded white card with subtle beige border.

Top eyebrow:

"PHILOSOPHY & STANDARDS"

Use small uppercase purple text.

Bold statement:

"Great systems balance mathematical simplicity with seamless user responsiveness. Backend resilience must meet effortless interface design."

Use strong medium-large typography.

Then paragraph:

"My career began with low-level systems programming in C and Python before expanding into cloud architecture and modern web frontends. Today, I build full-lifecycle digital platforms designed to survive rapid scaling while keeping developer velocity joyful and predictable."

Below this content:

Horizontal divider.

Three small information columns:

EDUCATION
- Stanford University
- M.S. Computer Science

FOCUS
- Distributed Cloud
- Low-latency services

PASSION
- UI Craft & DX
- Modular design kits

Each column should include a small purple line icon.

---

# 10. Statistics Grid

Create four statistic cards.

Grid:

2 columns × 2 rows.

Cards:

### Card 1

7+

Years Experience

Scaling tech from seed to hyper-growth.

### Card 2

40+

Shipped Projects

Mission-critical systems & tools.

### Card 3

3.2M+

Monthly Users

Supported across live products.

### Card 4

99.8%

Test Standard

Automated CI/CD regression gates.

Card style:

- White background
- Beige border
- Rounded corners
- Purple large number
- Small dark heading
- Small muted description
- Generous internal padding

---

# 11. Technical Expertise Section

Create a section using the same overall container.

Top label:

"02 / CAPABILITIES"

Purple short horizontal line before the label.

Heading:

"Technical Expertise"

Subtitle:

"A battle-tested stack built for velocity, performance, and operational clarity."

---

# 12. Expertise Card Grid

Create a responsive grid.

Desktop layout:

Row 1:
- Core Languages
- Frontend & Web
- Cloud & Databases

Row 2:
- DevOps & Testing
- Architecture Paradigms spanning approximately two columns

Cards should have:

- Warm white/off-white background
- Beige border
- Rounded corners
- Purple minimal icon inside a light-purple rounded square
- Card title
- Short description
- Technology pills/tags

---

# 13. Expertise Cards

## Core Languages

Icon: code/programming icon

Description:

"Type-safe, high-concurrency systems and low-latency API backends."

Tags:

- TypeScript
- Python
- Go
- Rust
- SQL

---

## Frontend & Web

Icon: frontend/window icon

Description:

"Responsive, responsive architectures and pixel-accurate interactive experiences."

Tags:

- React 18 / Next.js
- Tailwind CSS
- Vue 3
- WebSockets
- HTML5/ARIA

---

## Cloud & Databases

Icon: cloud icon

Description:

"High-availability persistence, memory caches, and resilient deployments."

Tags:

- PostgreSQL
- Redis
- AWS (ECS, S3)
- Docker
- Kubernetes

---

## DevOps & Testing

Icon: terminal/automation icon

Description:

"Automated delivery, end-to-end integration, and continuous telemetry."

Tags:

- GitHub Actions
- Playwright
- Jest
- Prometheus

---

## Architecture Paradigms

Icon: connected-nodes architecture icon

Description:

"Designing clean systems engineered for maintainability, data integrity, and strict security compliance."

Tags:

- Event-Driven Architecture
- REST & gRPC
- Microservices
- Zero Trust Security
- Infrastructure as Code

This card spans approximately two standard grid columns on desktop.

---

# 14. Technology Pills

Technology tags should be compact rounded pills.

Style:

- White background
- Thin beige border
- Slightly rounded corners
- Small dark text
- Comfortable horizontal padding
- Small vertical padding

Do not use colorful badges for every technology.

Keep the visual language restrained.

---

# 15. Typography

Use a modern geometric/sans-serif font.

Recommended combination:

Primary:
- Inter
- Geist
- Plus Jakarta Sans
- or another similar modern sans-serif

Technical labels:
- JetBrains Mono
- IBM Plex Mono
- or another monospace font

Headings:
- Very bold
- Tight line-height
- Slightly condensed visual appearance

Body:
- Regular/medium weight
- Comfortable line-height

---

# 16. Color System

Use approximately:

Primary:
#6D28D9

Secondary purple:
#7C3AED

Coral accent:
#C85A45

Dark text:
#0B0F19

Body text:
#334155

Warm background:
#FAF9F6

Card background:
#FFFFFF

Border:
#E8E0D5

Muted text:
#64748B

Success:
#22C55E

The exact shades can be adjusted slightly to visually match the reference screenshots.

---

# 17. Responsive Behavior

Desktop:
- Hero is two columns
- About uses large card + statistics grid
- Expertise uses multi-column grid
- Header uses full navigation

Tablet:
- Reduce headline size
- Reduce horizontal gaps
- Maintain two-column hero where possible
- Expertise grid may become 2 columns

Mobile:
- Header collapses
- Hero becomes one column
- Image appears below hero copy
- About becomes one column
- Statistics become 1 or 2 columns
- Expertise cards become one column
- Architecture card no longer spans multiple columns
- Buttons may wrap
- Preserve generous vertical spacing

Never allow:
- Horizontal scrolling
- Text overlapping
- Buttons overflowing
- Cards becoming too narrow
- Hero image being cropped incorrectly

---

# 18. Interactions

Add subtle interactions:

- Navigation hover states
- Button hover lift
- Button gradient transition
- Technology pill hover
- Card hover with very subtle elevation
- Image card subtle scale on hover
- Smooth anchor scrolling
- Mobile navigation drawer

Avoid excessive animation.

Use short, professional transitions around 150–250ms.

---

# 19. Accessibility

Implement:

- Semantic HTML
- Proper heading hierarchy
- Accessible button labels
- Keyboard navigation
- Visible focus states
- Alt text for the hero photograph
- Sufficient color contrast
- Reduced-motion support

Icons must not be the only way to communicate important actions.

---

# 20. Important Implementation Rule

Do not convert the screenshot into a single background image.

Rebuild every visible UI element using:

- HTML
- CSS
- React components
- SVG/Lucide icons
- Responsive CSS

Only the actual photographic asset should be treated as an image.

The final result should visually resemble the supplied screenshots while remaining a real responsive website.