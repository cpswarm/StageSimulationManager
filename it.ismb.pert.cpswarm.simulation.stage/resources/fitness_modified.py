import rosbag, sys

if (len(sys.argv) != 4):
    print "Logistics fitness function calculator\n"
    print "\n"
    print "Usage:\n"
    print "fitness.py [bagfile.bag] [maximum simulation time] [box count]\n"
    sys.exit(1)

bag = rosbag.Bag(sys.argv[1])
max_time = float(sys.argv[2])

box_count = float(sys.argv[3])
start_time = float(bag.get_start_time())
#print "start_time="+str(start_time)

time_sum = 0
box_count_done = 0

for subtopic, msg, t in bag.read_messages("target_done"):
    time_sum += float(msg.header.stamp.secs) - start_time
#    print "msg_time="+str(float(msg.header.stamp.secs))

#    print "msg.header.stamp="+str(msg.header.stamp)
#    print "msg.header.stamp.secs="+str(msg.header.stamp.secs)
    box_count_done += 1

# force box_count to be atleast box_count_done
box_count = max(box_count, box_count_done)

# any boxes that are not moved are considered moved in the maximum time
time_sum += (box_count - box_count_done) * max_time

average_clipped = min(time_sum / box_count, max_time)
fitness = (max_time - average_clipped) / max_time * 100

#print "time_sum="+str(time_sum)
print "box_count_done="+str(box_count_done)
#print "box_count="+str(box_count)
#print "max_time="+str(max_time)

#print "average_clipped="+str(average_clipped)
#print "(max_time - average_clipped) = "+str(max_time - average_clipped)

print "fitness="+str(fitness)
