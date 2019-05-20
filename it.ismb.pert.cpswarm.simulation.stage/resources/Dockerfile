FROM dconzon/ros:1.0.3

COPY . /home/
WORKDIR /home/

RUN apt-get install psmisc

RUN mkdir Desktop

RUN mkdir Desktop/cpswarm

RUN mkdir Desktop/test

RUN mkdir Desktop/test/src

WORKDIR /home/Desktop/test/

RUN catkin init --workspace .

RUN cp -r /home/emergency_exit /home/Desktop/test/src

RUN cp /home/ros.sh /home/Desktop/test/

RUN /bin/bash -c "source /opt/ros/kinetic/setup.bash"

RUN /bin/bash ros.sh

WORKDIR /home/

RUN mvn -B validate

RUN mvn install -DskipTests

# install xvfb to create a virtual display for tests
# RUN apt update && apt install -y xvfb

ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64/

RUN keytool -noprompt -importcert -trustcacerts \
    -file pert-demoenergy-virtus.ismb.polito.it.pem -alias pert-demoenergy-virtus.ismb.polito.it \
    -storepass changeit -keystore -J-Duser.language=en $JAVA_HOME/jre/lib/security/cacerts
   
# create a virtual display with Xvfb and set DISPLAY before starting java
# RUN Xvfb :1 -screen 0 1024x768x16 & DISPLAY=:1.0 \

CMD java -jar /home/target/it.ismb.pert.cpswarm.simulation.stage-1.1.0-jar-with-dependencies.jar

