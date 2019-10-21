#!/bin/bash
source /opt/ros/kinetic/setup.bash
cd $HOME/ws/
catkin build
source $HOME/ws/devel/setup.bash
