FROM cpswarm/stage-simulation-manager:2.0.14

WORKDIR $HOME

RUN mkdir cpswarm \
			 ws \
			 ws/src \
		    ws/src/emergency_exit

COPY emergency_exit $HOME/ws/src/emergency_exit

COPY startup.sh $HOME

COPY Dockerfile $HOME

COPY ros.sh $HOME/ws/

COPY pert-demoenergy-virtus.ismb.polito.it.pem $HOME

WORKDIR $HOME/ws/

RUN catkin init --workspace .

RUN /bin/bash -c "source /opt/ros/kinetic/setup.bash"

RUN chmod +x ros.sh \
&& /bin/bash ros.sh

RUN echo "source $HOME/ws/devel/setup.bash" >> ~/.bashrc \
	&& /bin/bash -c "source ~/.bashrc"

WORKDIR $HOME

ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64/

RUN keytool -noprompt -importcert -trustcacerts \
    -file pert-demoenergy-virtus.ismb.polito.it.pem -alias pert-demoenergy-virtus.ismb.polito.it \
    -storepass changeit -keystore -J-Duser.language=en $JAVA_HOME/jre/lib/security/cacerts

ENTRYPOINT ["/root/startup.sh"]
CMD []
