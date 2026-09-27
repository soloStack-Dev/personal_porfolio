/*
 * Faleel H — portfolio
 *
 * Progressive enhancement only. Every feature here has a working no-JavaScript
 * fallback, and htmx is optional: the contact form is a real <form action="/contact"
 * method="post">, so if the htmx CDN is unreachable the submission still works.
 *
 * No jQuery. The DOM work here is a few dozen lines and a framework-free
 * implementation keeps the page honest and dependency-light.
 */
(function () {
  "use strict";

  var $ = function (selector, scope) { return (scope || document).querySelector(selector); };
  var $$ = function (selector, scope) {
    return Array.prototype.slice.call((scope || document).querySelectorAll(selector));
  };

  var prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

  /* ------------------------------------------------------------------ toasts */

  var toastHost = $("#toast-host");

  function toast(message, variant) {
    if (!toastHost) {
      return;
    }
    var node = document.createElement("div");
    node.className = "toast" + (variant ? " toast--" + variant : "");

    var icon = document.createElement("span");
    icon.className = "toast__icon";
    icon.innerHTML = toastIcons[variant] || toastIcons.info;

    var text = document.createElement("span");
    text.textContent = message;

    node.appendChild(icon);
    node.appendChild(text);
    toastHost.appendChild(node);

    window.setTimeout(function () {
      node.classList.add("toast--out");
      window.setTimeout(function () { node.remove(); }, 220);
    }, 3200);
  }

  var toastIcons = {
    info: '<svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="M12 11v5.5"/><path d="M12 7.6h.01"/></svg>',
    warn: '<svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="M12 7.5v5.5"/><path d="M12 16.4h.01"/></svg>',
    ok: '<svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6.5 9.5 17 4 11.5"/></svg>'
  };

  /* ------------------------------------------------- placeholder outbound links
   * The spec never supplied real URLs, so nothing here is a dead <a href="#">:
   * these are buttons that explain themselves instead of silently doing nothing.
   */

  document.addEventListener("click", function (event) {
    var trigger = event.target.closest("[data-placeholder-link]");
    if (!trigger) {
      return;
    }
    event.preventDefault();
    toast(trigger.dataset.placeholderLink + " isn't linked yet.", "warn");
  });

  /* ---------------------------------------------------------- clipboard: copy buttons */

  document.addEventListener("click", function (event) {
    var trigger = event.target.closest("[data-copy]");
    if (!trigger) {
      return;
    }
    event.preventDefault();
    var value = trigger.dataset.copy;

    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard.writeText(value).then(
        function () { toast(value + " copied to clipboard.", "ok"); },
        function () { toast("Copy failed — it is " + value + ".", "warn"); }
      );
      return;
    }
    toast(value, "info");
  });

  /* ------------------------------------------------------------------- header */

  var header = $("[data-site-header]");
  var toTop = $("[data-to-top]");

  function onScroll() {
    var y = window.scrollY || window.pageYOffset;
    if (header) {
      header.classList.toggle("is-stuck", y > 8);
    }
    if (toTop) {
      toTop.classList.toggle("is-visible", y > 320);
    }
  }

  window.addEventListener("scroll", onScroll, { passive: true });
  onScroll();

  if (toTop) {
    toTop.addEventListener("click", function () {
      window.scrollTo({ top: 0, behavior: prefersReducedMotion.matches ? "auto" : "smooth" });
    });
  }

  /* ------------------------------------------------------------- mobile drawer */

  var navToggle = $("[data-nav-toggle]");
  var nav = $("#site-nav");
  var iconOpen = $("[data-nav-icon-open]");
  var iconClose = $("[data-nav-icon-close]");

  function setNav(open) {
    if (!nav || !navToggle) {
      return;
    }
    nav.classList.toggle("is-open", open);
    navToggle.setAttribute("aria-expanded", String(open));
    navToggle.setAttribute("aria-label", open ? "Close navigation menu" : "Open navigation menu");
    if (iconOpen) { iconOpen.classList.toggle("is-hidden", open); }
    if (iconClose) { iconClose.classList.toggle("is-hidden", !open); }
  }

  if (navToggle && nav) {
    navToggle.addEventListener("click", function () {
      setNav(navToggle.getAttribute("aria-expanded") !== "true");
    });

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && nav.classList.contains("is-open")) {
        setNav(false);
        navToggle.focus();
      }
    });

    document.addEventListener("click", function (event) {
      if (nav.classList.contains("is-open")
          && !nav.contains(event.target)
          && !navToggle.contains(event.target)) {
        setNav(false);
      }
    });

    $$("[data-nav-link]", nav).forEach(function (link) {
      link.addEventListener("click", function () { setNav(false); });
    });

    window.addEventListener("resize", function () {
      if (window.innerWidth > 720) {
        setNav(false);
      }
    });
  }

  /* ------------------------------------------------- active nav link on scroll */

  var navLinks = $$("[data-nav-link]");
  var sections = navLinks
    .map(function (link) { return document.getElementById(link.dataset.section); })
    .filter(Boolean);

  if (sections.length && "IntersectionObserver" in window) {
    var spy = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) {
          return;
        }
        navLinks.forEach(function (link) {
          link.classList.toggle("is-active", link.dataset.section === entry.target.id);
        });
      });
    }, { rootMargin: "-45% 0px -50% 0px" });

    sections.forEach(function (section) { spy.observe(section); });
  }

  /* ---------------------------------------------------- 03 / PORTFOLIO filter */

  var filterButtons = $$("[data-filter]");
  var projectCards = $$("[data-project-grid] [data-filters]");
  var filterStatus = $("[data-filter-status]");

  function applyFilter(id, label) {
    var shown = 0;

    projectCards.forEach(function (card) {
      var match = id === "all" || card.dataset.filters.split(" ").indexOf(id) !== -1;
      card.hidden = !match;
      if (match) {
        shown += 1;
      }
    });

    filterButtons.forEach(function (button) {
      var active = button.dataset.filter === id;
      button.classList.toggle("is-active", active);
      button.setAttribute("aria-pressed", String(active));
    });

    if (filterStatus) {
      filterStatus.textContent = shown + (shown === 1 ? " project" : " projects")
        + " · " + label;
    }
  }

  filterButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      applyFilter(button.dataset.filter, button.textContent.trim());
    });
  });

  /* ---------------------------------------------- 04 / DIALOGUE: htmx lifecycle */

  var form = $(".form");
  var result = $("#contact-result");

  function clearFieldErrors() {
    $$(".field__error").forEach(function (node) {
      node.textContent = "";
      node.classList.remove("is-error");
    });
  }

  if (form) {
    // Only relevant when htmx actually loaded; harmless otherwise.
    document.body.addEventListener("htmx:afterRequest", function (event) {
      var detail = event.detail;
      if (!detail || !detail.elt || detail.elt !== form || !detail.successful) {
        return;
      }

      var swapped = $("#contact-result");
      var succeeded = swapped && swapped.classList.contains("is-success");

      if (succeeded) {
        form.reset();
        clearFieldErrors();
        toast("Thanks — your message is on its way.", "ok");
      }
      if (swapped) {
        swapped.scrollIntoView({
          block: "nearest",
          behavior: prefersReducedMotion.matches ? "auto" : "smooth"
        });
      }
    });

    // Belt and braces for the OOB slots: drop stale messages as soon as a field is edited.
    form.addEventListener("input", function (event) {
      var field = event.target.closest(".field");
      if (!field) {
        return;
      }
      var slot = field.querySelector(".field__error");
      if (slot && slot.classList.contains("is-error")) {
        slot.textContent = "";
        slot.classList.remove("is-error");
      }
    });
  }

  if (result) {
    // Nothing to reveal by hand: the server decides visibility, and a hidden panel stays hidden.
    result.setAttribute("aria-busy", "false");
  }
})();
