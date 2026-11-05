#!/usr/bin/env python3
"""
SONZA - Mathematical & Algorithmic Verification Suite
"""

import math
import re
import sys

def test_lrc_parser():
    print("Testing 1. Synchronized LRC Parser...")
    raw_lrc = """[00:02.50]Close your eyes and listen
[00:07.10]<00:07.10>Every <00:08.20>beat <00:09.50>matters
[00:15.00]Hear music differently"""

    line_pattern = re.compile(r"\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)")
    word_pattern = re.compile(r"<(\d{2}):(\d{2})\.(\d{2,3})>([^<]*)")

    lines = []
    for line in raw_lrc.strip().split("\n"):
        m = line_pattern.match(line.strip())
        assert m, f"Line didn't match: {line}"
        mins = int(m.group(1))
        secs = int(m.group(2))
        ms_str = m.group(3)
        ms = int(ms_str) * (10 if len(ms_str) == 2 else 1)
        start_ms = (mins * 60 * 1000) + (secs * 1000) + ms
        text = m.group(4).strip()

        words = []
        for wm in word_pattern.finditer(text):
            w_min, w_sec, w_ms_str, word = int(wm.group(1)), int(wm.group(2)), wm.group(3), wm.group(4)
            w_ms = int(w_ms_str) * (10 if len(w_ms_str) == 2 else 1)
            w_start = (w_min * 60 * 1000) + (w_sec * 1000) + w_ms
            words.append((word, w_start))

        lines.append({"start_ms": start_ms, "text": text, "words": words})

    assert len(lines) == 3
    assert lines[0]["start_ms"] == 2500
    assert lines[1]["start_ms"] == 7100
    assert len(lines[1]["words"]) == 3
    assert lines[1]["words"][0][0].strip() == "Every"
    assert lines[1]["words"][0][1] == 7100
    print("  ✓ LRC timestamp & word-sync parser verified!")

def test_equalizer_math():
    print("Testing 2. 10-Band Equalizer Linear Multiplier Math...")
    def calc_gain(db: float, preamp: float = 0.0) -> float:
        return 10.0 ** ((db + preamp) / 20.0)

    assert abs(calc_gain(0.0) - 1.0) < 1e-4
    assert abs(calc_gain(6.0) - 1.99526) < 1e-3
    assert abs(calc_gain(-6.0) - 0.50118) < 1e-3
    assert abs(calc_gain(12.0) - 3.98107) < 1e-3
    assert abs(calc_gain(-12.0) - 0.25118) < 1e-3
    print("  ✓ 10-band equalizer linear gain formula verified!")

def test_replay_gain_and_limiter():
    print("Testing 3. ReplayGain Normalization & Anti-Clipping Limiter...")
    def calculate_scale(track_gain_db: float, peak: float) -> float:
        linear_scale = 10.0 ** (track_gain_db / 20.0)
        if linear_scale * peak > 1.0:
            linear_scale = 1.0 / peak
        return linear_scale

    scale1 = calculate_scale(-3.0, 0.95)
    assert abs(scale1 - 0.7079) < 1e-3

    scale2 = calculate_scale(6.0, 0.90)
    assert abs(scale2 - (1.0 / 0.90)) < 1e-3
    print("  ✓ ReplayGain & anti-clipping limiter verified!")

def test_constant_power_crossfade():
    print("Testing 4. Constant-Power Crossfade Acoustic Energy...")
    def crossfade(progress: float):
        angle = progress * (math.pi / 2.0)
        return math.cos(angle), math.sin(angle)

    out0, in0 = crossfade(0.0)
    assert abs(out0 - 1.0) < 1e-4 and abs(in0 - 0.0) < 1e-4

    out_half, in_half = crossfade(0.5)
    total_power = (out_half ** 2) + (in_half ** 2)
    assert abs(total_power - 1.0) < 1e-4, f"Acoustic power drop detected: {total_power}"

    out1, in1 = crossfade(1.0)
    assert abs(out1 - 0.0) < 1e-4 and abs(in1 - 1.0) < 1e-4
    print("  ✓ Constant-power crossfade curve verified!")

def test_spotify_fuzzy_matcher():
    print("Testing 5. Spotify Metadata Fuzzy Matcher...")
    def levenshtein(s1: str, s2: str) -> int:
        if len(s1) < len(s2):
            return levenshtein(s2, s1)
        if len(s2) == 0:
            return len(s1)
        prev = range(len(s2) + 1)
        for i, c1 in enumerate(s1):
            curr = [i + 1]
            for j, c2 in enumerate(s2):
                insert = prev[j + 1] + 1
                delete = curr[j] + 1
                replace = prev[j] + (0 if c1 == c2 else 1)
                curr.append(min(insert, delete, replace))
            prev = curr
        return prev[-1]

    def sanitize(s: str) -> str:
        s_no_parens = re.sub(r"\(.*?\)|\ fusion.*|\[.*?\]", "", s.lower())
        return re.sub(r"[^a-z0-9 ]", "", s_no_parens).strip()

    def similarity(s1: str, s2: str) -> float:
        c1 = sanitize(s1)
        c2 = sanitize(s2)
        if c1 == c2: return 1.0
        if not c1 or not c2: return 0.0
        if c1 in c2 or c2 in c1:
            return max(0.80, min(len(c1), len(c2)) / max(len(c1), len(c2)))
        dist = levenshtein(c1, c2)
        return max(0.0, 1.0 - (dist / max(len(c1), len(c2))))

    sim1 = similarity("Midnight Horizon (Original Mix)", "Midnight Horizon")
    assert sim1 >= 0.80, f"Expected match, got {sim1}"

    sim2 = similarity("Velvet Nights", "Velvet Nights")
    assert sim2 == 1.0

    sim3 = similarity("Midnight Horizon", "Classical Sonata No 5")
    assert sim3 < 0.30
    print("  ✓ Spotify playlist fuzzy matching algorithm verified!")

if __name__ == "__main__":
    print("=== SONZA Core Verification Test Suite ===")
    test_lrc_parser()
    test_equalizer_math()
    test_replay_gain_and_limiter()
    test_constant_power_crossfade()
    test_spotify_fuzzy_matcher()
    print("\n==========================================")
    print("ALL CORE ALGORITHMIC TEST SUITES PASSED!")
    print("==========================================")
