// This script ensures that no cached basic-auth credentials remain in the browser
window.addEventListener("load", function() {
  function buildBasicAuthHeader(user, pwd) {
    return "Basic " + btoa(user + ":" + pwd);
  }

  $.ajax({
    type: "GET",
    url: CONTEXT_PATH + "js/deny.js",
    dataType: "json",
    async: false,
    headers: {
      // Basic YmFkOmFhYQ==
      "Authorization": buildBasicAuthHeader("bad", "aaa")
    },
    error: function() {
      $.ajax({
        type: "GET",
        url: CONTEXT_PATH + "js/deny.js",
        dataType: "json",
        async: false,
        headers: {
          // Overwrite Pentaho realm credentials with these special ones that this endpoint always accepts.
          // Basic X19fOl9fXw==
          "Authorization": buildBasicAuthHeader("___", "___")
        }
      });
    },
  });
});
