# Contact Section — Web Building Prompt

Create the fourth major section of the portfolio website.

Section identifier:

04 / DIALOGUE

Section title:

Let's Connect

Subtitle:

Have a project in mind, an architectural challenge, or want to say hello?

The section must maintain the same design system as Hero, About, Expertise, and Projects.

---

# 1. Section Header

Technical label:

04 / DIALOGUE

Preceded by a short horizontal purple line.

Heading:

Let's Connect

Subtitle:

Have a project in mind, an architectural challenge, or want to say hello?

Typography must match previous section headings.

---

# 2. Main Contact Layout

Desktop:

Two-column layout.

LEFT:
Direct Contact card.

RIGHT:
Contact form card.

Approximate ratio:

Left:
40%

Right:
60%

Gap:
24px.

---

# 3. Direct Contact Card

Card:

- Warm off-white/cream background
- Thin beige border
- Rounded corners
- Generous padding

Heading:

Direct Contact

Description:

I typically respond within one business day for consulting, technical architecture advisory, and engineering queries.

---

# 4. Contact Information Items

Create three horizontal information cards.

## Email

Icon:
Envelope.

Label:

EMAIL

Value:

alex.rivera@example.dev

---

## Location

Icon:
Location pin.

Label:

LOCATION

Value:

San Francisco, CA / Remote Worldwide

Use orange accent for the location icon.

---

## Availability

Icon:
Calendar.

Label:

CURRENT AVAILABILITY

Value:

Accepting Q3 / Q4 projects

---

Each contact item:

- White background
- Thin warm beige border
- Rounded corners
- Icon in a small light-purple rounded square
- Small uppercase label
- Value underneath
- Comfortable vertical spacing

---

# 5. Public Key Area

At the bottom of the Direct Contact card:

Create a small horizontal security/key panel.

Left:

PGP: 4A91 2E78 FC02 91BC

Right:

Public Key

Use tiny monospace typography.

The Public Key text should be purple.

---

# 6. Contact Form

Right-side card:

White background.

Thin beige border.

Rounded corners.

Large internal padding.

Use a two-column form row at the top.

Column 1:

Full Name *

Input:
Elena Rostova

Column 2:

Email Address *

Input:
elena@example.com

---

# 7. Topic Dropdown

Label:

Topic of Interest

Dropdown value:

Project Inquiry & Architecture

Include a downward chevron.

---

# 8. Message Field

Label:

Message Details *

Textarea placeholder:

Tell me about your goals, stack, timeline...

Large textarea.

---

# 9. Submit Button

Button text:

Send Message

Include send/paper-plane icon.

Style:

Purple → coral gradient.

White text.

Rounded pill.

Subtle shadow.

Hover:

Very small upward movement and slightly stronger shadow.

---

# 10. Form Behavior

Implement real form interactions:

- Required field validation
- Email validation
- Topic selection
- Message validation
- Submit loading state
- Success message
- Error message

Do not expose the PGP key as a fake security mechanism if this is only a portfolio UI.

If no backend is connected, use a clearly structured placeholder submit handler.

---

# 11. Responsive

Desktop:
two columns.

Tablet:
two columns if enough space.

Mobile:
stack Direct Contact above Contact Form.

Form fields:

Desktop:
Name and Email side-by-side.

Mobile:
stack vertically.

The form must remain comfortable to use on touch screens.