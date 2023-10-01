# Satellite_Space_Collision_prevision

<h2>Description</h2>

This project is about implementing a tablet application to display satellites and more information to the liquid galaxy.
In this application, you can select 2 satellites and click on the check collision button, and you will be able to see in the liquid galaxy: if they will collide in a future, displaying in the liquid galaxy the two satellite orbits, in which zone the collision will be done, information of the two satellites in the right screen and in the left screen we have some logos (app logo, liquid galaxy lab logo, liquid galaxy EU logo…).
When you click the check collision button, the liquid galaxy will fly to the point of the earth that the satellites is probably that they collide. As you click this button, it makes a post request to the server, and it returns the response of an artificial intelligence model that I’ve created that predicts if those 2 satellites will collide or not.

Once you have selected the satellites, you can press the button play in the Visualization area, that starts doing a tour through the orbit of 1 of the satellites displayed. In this area we have a back button and a next button, this buttons makes a fly to the first position and last position of 1 satellite.

Then, you have a visualization of multiple orbits area, in this area you have 4 buttons, tiny, small medium and large. Those buttons show multiple satellites orbits in the liquid galaxy, from tiny to large, this will be displaying more orbit satellites in the liquid galaxy.

<h2>Deploying app</h2>
<h3>Server</h3>

To use this app you had to install 2 dockerfiles:

<h4>Dockerfile that predict the collision of 2 satellites:</h4>


<ul><p>docker pull rafelss/satellite_collision:1.0.0</p>
  <p>docker images</p>
<p>docker run -p 8080:8080 "image id"</p>
  </ul>
  The numbers after -p are the ports that you had to put in the configuration menu of the application.
  Ex: docker run -p 8080:8080 "image id", in the application you had to write the port 8080.

<h4>Dockerfile that creates the orbits of the satellites:</h4>


<ul><p>docker pull rafelss/coordinates_rafelss:latest</p>
  <p>docker images</p>
<p>docker run -p 8081:8081 "image id"</p>

  </ul>
