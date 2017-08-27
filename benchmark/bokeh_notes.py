cats = [str(r) for r in df["Users"].unique()]

colors = ["dodgerblue", "limegreen"]

dino = df.query('Server == "Dinosaur-CGI"')
node = df.query('Server == "Node-Express"')


p1 = figure(tools="save", title="Dinosaur-CGI response times under load (median, 99th percentile, max)", x_range=cats, x_axis_label="Connections", y_axis_label="Response time (ms)", width=650, height=300, y_axis_type="log", y_range=(10,12000))

p1.circle(cats, dino["50th (ms)"], size=15, fill_alpha=0, line_width=2, color=colors[0])
p1.circle(cats, dino["99th (ms)"], size=8, color=colors[0])
p1.rect(cats, dino["max (ms)"], 0.4, 0.1, color=colors[0])

p1.segment(cats, dino["50th (ms)"], cats, dino["99th (ms)"], line_width=3, color=colors[0])
p1.segment(cats, dino["99th (ms)"], cats, dino["max (ms)"], line_width=2, color=colors[0])

dino_error_rate_size = (15 * dino["error rate"])
# p1.circle(cats, dino["mean (ms)"], size=dino_error_rate_size, color="red")

p2 = figure(tools="save", title="NodeJS response times under load (median, 99th percentile, max)", x_range=cats, x_axis_label="Connections", y_axis_label="Response time (ms)", width=650, height=300, y_axis_type="log", y_range=(10,12000))

p2.circle(cats, node["50th (ms)"], size=15, fill_alpha=0, line_width=2, color=colors[1])
p2.circle(cats, node["99th (ms)"],size=8, color=colors[1])
p2.rect(cats, node["max (ms)"], 0.4, 0.1, color=colors[1])

p2.segment(cats, node["50th (ms)"], cats, node["99th (ms)"], line_width=3, color=colors[1])
p2.segment(cats, node["99th (ms)"], cats, node["max (ms)"], line_width=2, color=colors[1])

node_error_rate_size = (15 * node["error rate"])
# p2.circle(cats, node["mean (ms)"], size=node_error_rate_size, color="red")

show(p1)
show(p2)
