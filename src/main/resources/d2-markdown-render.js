// Copyright 2025 D2 Diagram Plugin. Apache 2.0 license.
// Injected into the IntelliJ Markdown JCEF preview.
//
// Timing notes (based on MarkdownJCEFHtmlPanel internals):
//   1. This script executes as an inline <script> during HTML parsing.
//   2. After the page fully loads, JcefBrowserPipeImpl sets up the message
//      bridge and dispatches window.IdeReady — only then can we post messages.
//   3. Markdown content is patched into the DOM via Incremental DOM.
//      IncrementalDOM.notifications.afterPatchListeners is the correct hook
//      for detecting content changes — the same mechanism used by MathJax and
//      ScrollSync in the platform.  setTimeout / MutationObserver are not needed.
(function () {
  'use strict';

  var ideReady = false;
  var pendingRequests = {};
  var requestCounter = 0;

  // Receive rendered SVG (or error) back from Kotlin
  window.__IntelliJTools.messagePipe.subscribe('d2/result', function (data) {
    try {
      var msg = JSON.parse(data);
      var callback = pendingRequests[msg.id];
      if (callback) {
        delete pendingRequests[msg.id];
        callback(msg.svg || null, msg.error || null);
      }
    } catch (e) {
      console.error('[D2] Failed to parse result:', e);
    }
  });

  // D2 CLI outputs SVGs with pixel-exact width/height matching the viewBox
  // (e.g. viewBox="0 0 255 600" → width="255" height="600"), which renders too
  // large next to Markdown text. Scale to 70% so diagrams are proportional.
  // Tune this if diagrams look too big or too small.
  var D2_SCALE = 0.7;

  // Replaces width/height on the root <svg> with values scaled from the viewBox.
  // e.g. viewBox="0 0 255 600" → <svg width="179" height="420"> (at 70%)
  function setSvgNaturalSize(svg) {
    var vb = svg.match(/viewBox="[\d.+-]+\s+[\d.+-]+\s+([\d.]+)\s+([\d.]+)"/);
    if (!vb) return svg;
    var w = Math.round(parseFloat(vb[1]) * D2_SCALE);
    var h = Math.round(parseFloat(vb[2]) * D2_SCALE);
    var s = svg.replace(/(<svg\b[^>]*?)\s+width="[^"]*"/, '$1');
    s = s.replace(/(<svg\b[^>]*?)\s+height="[^"]*"/, '$1');
    return s.replace(/(<svg\b[^>]*?)>/, '$1 width="' + w + '" height="' + h + '">');
  }

  function renderD2Blocks() {
    if (!ideReady) return;
    document.querySelectorAll('code.language-d2').forEach(function (el) {
      if (el.dataset.d2Rendered) return;
      el.dataset.d2Rendered = 'pending';

      var pre = el.closest('pre') || el.parentElement;
      var source = el.textContent;
      var id = String(requestCounter++);

      pendingRequests[id] = function (svg, error) {
        if (error) {
          el.dataset.d2Rendered = 'error';
          var box = document.createElement('pre');
          box.style.cssText =
            'color:#c00;border:1px solid #c00;border-radius:4px;' +
            'padding:8px;font-size:12px;white-space:pre-wrap;margin:4px 0;';
          box.textContent = '\u26A0 D2: ' + error;
          pre.replaceWith(box);
        } else {
          var host = document.createElement('div');
          // Shadow DOM isolates D2's embedded <style> tags from the page.
          var shadow = host.attachShadow({ mode: 'open' });
          // Set intrinsic pixel dimensions from the SVG's viewBox so the browser
          // renders it at its natural size rather than stretching to fill the column.
          // max-width:100% still allows it to shrink on narrow screens.
          shadow.innerHTML =
            '<style>svg { max-width: 100%; height: auto; display: block; }</style>' +
            setSvgNaturalSize(svg);
          pre.replaceWith(host);
        }
      };

      window.__IntelliJTools.messagePipe.post(
        'd2/render',
        JSON.stringify({ id: id, source: source })
      );
    });
  }

  // IdeReady fires once the Kotlin JCEF bridge is fully set up.
  // Only after this event can we safely call messagePipe.post().
  window.addEventListener('IdeReady', function () {
    ideReady = true;
    renderD2Blocks();
  });

  // IncrementalDOM.notifications.afterPatchListeners is the platform-correct
  // hook for reacting to Markdown content changes.  Every time the preview
  // re-renders (user edits, file switch, scroll), this callback fires.
  // This is the same mechanism used by MathJax and ScrollSync.
  IncrementalDOM.notifications.afterPatchListeners.push(function () {
    renderD2Blocks();
  });
})();
