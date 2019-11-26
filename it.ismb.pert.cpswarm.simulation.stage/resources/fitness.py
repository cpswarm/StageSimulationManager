import rosbag, sys

if (len(sys.argv) != 3):
    print "Logistics fitness function calculator\n"
    print "\n"
    print "Usage:\n"
    print "fitness.py [bagfile.bag] [maximum simulation time]\n"
    sys.exit(1)

bag = rosbag.Bag(sys.argv[1])
max_time = float(sys.argv[2])
start_time = float(bag.get_start_time())

time_sum = 0
box_count = 0

for subtopic, msg, t in bag.read_messages("target_done"):
    time_sum += float(msg.header.stamp.secs) - start_time
    box_count += 1

average_clipped = min(time_sum / box_count, max_time)
fitness = (max_time - average_clipped) / max_time * 100

print fitness
