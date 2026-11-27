
import csv
import re
import pathlib

SNAPSHOTS = ["phase3", "phase4-refactored", "phase4"]

CLASSES = [
    "OrderService",
    "DeliveryService",
    "OrderController",
    "MapService",
    "CourierProfile"
]

METHODS = [
    "OrderService::placeOrder",
    "DeliveryService::confirmPickup",
    "DeliveryService::confirmDelivery",
    "DeliveryService::reassign",
    "MapService::courierPoint"
]


def name_of(path):
    return re.split(r"[\\/]", path)[-1].rsplit(".", 1)[0]


def load(snapshot):
    base = pathlib.Path("docs/metrics") / snapshot
    files, funcs = {}, {}

    with open(base / "lizard-output.csv", newline="", encoding="utf-8-sig") as f:
        for row in csv.reader(f):
            if len(row) < 8:
                continue

            try:
                nloc, ccn = int(row[0]), int(row[1])
            except ValueError:
                continue

            path, func = row[6], row[7]

            entry = files.setdefault(
                name_of(path),
                {"methods": 0, "ccn": 0, "max": 0, "nloc": 0}
            )

            entry["methods"] += 1
            entry["ccn"] += ccn
            entry["max"] = max(entry["max"], ccn)
            funcs[func] = (nloc, ccn)

    pat = (
        r"\s*(\d+)\s+[\d.]+\s+[\d.]+\s+[\d.]+\s+"
        r"\d+\s+(\S.*\.(?:java|jsx?))\s*$"
    )

    with open(base / "lizard-output.txt", encoding="utf-8-sig",
              errors="ignore") as f:
        for line in f:
            m = re.match(pat, line)
            if m and "@" not in m.group(2):
                files.setdefault(
                    name_of(m.group(2)),
                    {"methods": 0, "ccn": 0, "max": 0, "nloc": 0}
                )["nloc"] = int(m.group(1))

    return files, funcs


data = {
    s: load(s)
    for s in SNAPSHOTS
    if (pathlib.Path("docs/metrics") / s / "lizard-output.csv").exists()
}

labels = {
    "phase3": "PHASE 3",
    "phase4-refactored": "PHASE 4 REFACTORED",
    "phase4": "PHASE 4"
}


def print_table(headers, rows, widths):
    separator = " | ".join("-" * w for w in widths)

    print()
    print("   ".join(
        f"{header:<{width}}"
        for header, width in zip(headers, widths)
    ))
    print(separator)

    for row in rows:
        print("   ".join(
            f"{str(value):<{width}}"
            for value, width in zip(row, widths)
        ))


# CLASS-LEVEL METRICS
print("\n" + "=" * 105)
print("                         CLASS-LEVEL METRICS")
print("=" * 105)

headers = ["CLASS", "METRIC"] + [labels[s] for s in data]
widths = [24, 22] + [24] * len(data)

rows = []

metrics = [
    ("NLOC", "nloc"),
    ("Methods", "methods"),
    ("Total CCN", "ccn"),
    ("Max Method CCN", "max")
]

for c in CLASSES:
    for i, (label, key) in enumerate(metrics):
        values = [
            str(d[0].get(c, {}).get(key, "-"))
            for d in data.values()
        ]

        rows.append([c if i == 0 else "", label] + values)

    rows.append(["", "", *[""] * len(data)])

print_table(headers, rows, widths)


# METHOD-LEVEL METRICS
print("\n" + "=" * 130)
print("                         METHOD-LEVEL METRICS")
print("=" * 130)

headers = ["CLASS", "METHOD"]

for s in data:
    headers.extend([
        f"{labels[s]} NLOC",
        f"{labels[s]} CCN"
    ])

widths = [24, 32] + [24, 18] * len(data)

rows = []

for method in METHODS:
    class_name, method_name = method.split("::")
    values = [class_name, method_name]

    for d in data.values():
        if method in d[1]:
            nloc, ccn = d[1][method]
            values.extend([str(nloc), str(ccn)])
        else:
            values.extend(["-", "-"])

    rows.append(values)

print_table(headers, rows, widths)

print("\n" + "=" * 130)
