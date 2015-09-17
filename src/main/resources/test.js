<!DOCTYPE html>
<html>
<body>

<script>
var eventSource = new EventSource("http://localhost:8080/sse");
eventSource.onmessage = function(event)
{
    window.console.info("Server-Sent Event: " + event.data);
};
</script>


<h1>My First JavaScript</h1>

<button type="button"
onclick="document.getElementById('demo').innerHTML = Date()">
Click me to display Date and Time.</button>

<p id="demo"></p>

</body>
</html> 
