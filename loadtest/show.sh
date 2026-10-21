#!/usr/bin/env python3
"""
Viewer and summary tool for Munchies load test results.
Usage:
  ./loadtest/show.sh                 # View latest run details
  ./loadtest/show.sh --list          # List all available runs
  ./loadtest/show.sh <timestamp>     # View a specific run
"""

import sys
import os
import json
import csv
import glob

RESULTS_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "results")

def format_num(val, dec=1):
    if val is None or not isinstance(val, (int, float)):
        return "n/a"
    return f"{val:.{dec}f}"

def format_pct(val):
    if val is None or not isinstance(val, (int, float)):
        return "n/a"
    return f"{val * 100:.2f}%"

def get_runs():
    if not os.path.isdir(RESULTS_DIR):
        return []
    runs = []
    for entry in sorted(os.listdir(RESULTS_DIR)):
        run_path = os.path.join(RESULTS_DIR, entry)
        if os.path.isdir(run_path):
            runs.append(entry)
    return runs

def parse_run(run_name):
    run_dir = os.path.join(RESULTS_DIR, run_name) if not os.path.isdir(run_name) else run_name
    summary_path = os.path.join(run_dir, "summary.json")
    scaling_path = os.path.join(run_dir, "scaling.csv")
    k6_log_path = os.path.join(run_dir, "k6.log")
    hpa_path = os.path.join(run_dir, "hpa-events.txt")

    summary_data = None
    if os.path.exists(summary_path):
        try:
            with open(summary_path) as f:
                summary_data = json.load(f)
        except Exception:
            pass

    scaling_rows = []
    if os.path.exists(scaling_path):
        try:
            with open(scaling_path) as f:
                reader = csv.DictReader(f)
                for r in reader:
                    scaling_rows.append(r)
        except Exception:
            pass

    hpa_text = ""
    if os.path.exists(hpa_path):
        try:
            with open(hpa_path) as f:
                hpa_text = f.read()
        except Exception:
            pass

    target_url = "n/a"
    if os.path.exists(k6_log_path):
        try:
            with open(k6_log_path) as f:
                for line in f:
                    if "target ............." in line:
                        target_url = line.split("target .............")[1].strip()
                        break
        except Exception:
            pass

    return {
        "run_id": os.path.basename(run_dir),
        "dir": run_dir,
        "target": target_url,
        "summary": summary_data,
        "scaling": scaling_rows,
        "hpa": hpa_text,
    }

def print_runs_list():
    runs = get_runs()
    if not runs:
        print(f"No test results found in {RESULTS_DIR}")
        return

    print("=" * 95)
    print(f"{'RUN ID':<20} {'ENDPOINT':<14} {'REQS':<8} {'THROUGHPUT':<14} {'P95 LAT':<12} {'ERR RATE':<10} {'STATUS'}")
    print("-" * 95)

    for r_name in runs:
        data = parse_run(r_name)
        s = data["summary"]
        target = data.get("target", "")
        if "auth/verify" in target:
            ep = "jwt-verify"
        elif "health" in target:
            ep = "health"
        else:
            ep = target.split(":")[-1].split("/")[-1] if target != "n/a" else "unknown"

        if s and "metrics" in s:
            m = s["metrics"]
            reqs = m.get("iterations", {}).get("values", {}).get("count", "n/a")
            rate = format_num(m.get("http_reqs", {}).get("values", {}).get("rate"), 1) + " req/s"
            p95 = format_num(m.get("http_req_duration", {}).get("values", {}).get("p(95)"), 1) + " ms"
            err = format_pct(m.get("http_req_failed", {}).get("values", {}).get("rate"))
            status = "Completed"
        else:
            reqs = "n/a"
            rate = "n/a"
            p95 = "n/a"
            err = "n/a"
            status = "Incomplete / Failed"
        print(f"{r_name:<20} {ep:<14} {str(reqs):<8} {rate:<14} {p95:<12} {err:<10} {status}")
    print("=" * 95)

def display_run(run_name):
    data = parse_run(run_name)
    s = data["summary"]

    print("\n" + "=" * 60)
    print(f" LOAD TEST RUN: {data['run_id']}")
    print(f" Target URL:   {data.get('target', 'n/a')}")
    print(f" Directory:    {data['dir']}")
    print("=" * 60)

    if s and "metrics" in s:
        m = s["metrics"]
        val = lambda name, k: m.get(name, {}).get("values", {}).get(k)
        print("\n--- Performance Summary ---")
        print(f"  Total Requests:      {val('iterations', 'count') or val('http_reqs', 'count') or 'n/a'}")
        print(f"  Throughput:          {format_num(val('http_reqs', 'rate'), 2)} req/s")
        print(f"  Latency (avg):       {format_num(val('http_req_duration', 'avg'))} ms")
        print(f"  Latency (med / p90): {format_num(val('http_req_duration', 'med'))} ms / {format_num(val('http_req_duration', 'p(90)'))} ms")
        print(f"  Latency (p95 / p99): {format_num(val('http_req_duration', 'p(95)'))} ms / {format_num(val('http_req_duration', 'p(99)'))} ms")
        print(f"  Latency (max):       {format_num(val('http_req_duration', 'max'))} ms")
        print(f"  Error Rate:          {format_pct(val('http_req_failed', 'rate'))}")
    else:
        print("\n  [!] No valid summary.json found for this run (run may have failed or aborted).")

    # Scaling timeline
    rows = data["scaling"]
    if rows:
        print("\n--- Scaling Timeline (sampled every 10s) ---")
        print(f"  {'TIME':<10} {'REPLICAS':<10} {'CPU %':<10} {'GRAPH'}")
        print("  " + "-" * 55)
        for r in rows:
            ts = r.get("timestamp", "")
            reps = r.get("ready_replicas", "")
            cpu = r.get("cpu_percent", "")
            
            # Simple bar visualization
            reps_num = int(reps) if reps.isdigit() else 0
            cpu_num = int(cpu) if cpu.isdigit() else 0
            
            reps_bar = "█" * reps_num
            cpu_bar = "▒" * (cpu_num // 10)
            print(f"  {ts:<10} {reps or '?':<10} {cpu or '?':<10}%  Replicas:[{reps_bar:<4}] CPU:[{cpu_bar:<10}]")
    else:
        print("\n  [i] No scaling samples recorded.")

    # Events summary
    if data["hpa"]:
        events = [line for line in data["hpa"].splitlines() if "Scaled" in line or "SuccessfulCreate" in line or "SuccessfulDelete" in line]
        if events:
            print("\n--- Autoscaling Events ---")
            for e in events[-10:]:
                print(f"  {e}")

    print("\n" + "=" * 60 + "\n")

def main():
    args = sys.argv[1:]
    if "--list" in args or "-l" in args:
        print_runs_list()
        return

    target_run = None
    if args:
        for a in args:
            if not a.startswith("-"):
                target_run = a
                break

    if not target_run or target_run == "latest":
        runs = get_runs()
        if not runs:
            print(f"No test results found under {RESULTS_DIR}.")
            print("Run a load test first with: ./loadtest/run.sh [--fast]")
            return
        target_run = runs[-1]

    display_run(target_run)

if __name__ == "__main__":
    main()
