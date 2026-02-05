import re
import sys
import json
import random
import time
from playwright.sync_api import sync_playwright

def verify_infrastructure():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        # Enable request/response logging
        page.on("console", lambda msg: print(f"CONSOLE: {msg.text}"))

        try:
            # Registration
            print("Navigating to register page...")
            page.goto("http://localhost:3000/register", timeout=30000)

            print("Waiting for country select...")
            page.wait_for_selector("select[name='countryId']", timeout=30000)

            # Check if countries are loaded
            time.sleep(2)
            options = page.query_selector_all("select[name='countryId'] option")

            country_val = ""
            if len(options) > 1:
                country_val = page.eval_on_selector("select[name='countryId']", "el => el.options[1].value")
                print(f"Selected country ID: {country_val}")
                page.select_option("select[name='countryId']", country_val)
            else:
                 print("ERROR: No countries available to select.")
                 sys.exit(1)

            username = f"user_{int(time.time())}"
            print(f"Registering user: {username}")

            page.fill("input[name='username']", username)
            page.fill("input[name='email']", f"{username}@example.com")
            page.fill("input[name='emailConfirm']", f"{username}@example.com")
            page.fill("input[name='password']", "password")
            page.fill("input[name='passwordConfirm']", "password")
            page.fill("input[name='clubName']", f"{username} FC")

            print("Submitting registration...")
            with page.expect_response("**/api/user/register") as response_info:
                page.click("button[type='submit']")

            reg_response = response_info.value
            print(f"Registration API status: {reg_response.status}")
            if reg_response.status != 200:
                 print("Registration failed.")
                 sys.exit(1)

            page.wait_for_url("http://localhost:3000/", timeout=10000)
            print("Registered and logged in successfully.")

            print("Navigating to Infrastructure page...")
            page.goto("http://localhost:3000/infrastructure")

            print("Waiting for Infrastructure header...")
            page.wait_for_selector("h2:has-text('Infrastructure Management')", timeout=15000)
            print("Infrastructure header found.")

            # Wait a bit for the facilities to render properly (animations etc)
            time.sleep(2)

            # Take success screenshot
            page.screenshot(path="verification/infrastructure_dashboard.png", full_page=True)
            print("Screenshot saved to verification/infrastructure_dashboard.png")

        except Exception as e:
            print(f"Caught exception: {e}")
            page.screenshot(path="verification_failure.png")
            print("Screenshot saved to verification_failure.png")
            sys.exit(1)
        finally:
            browser.close()

if __name__ == "__main__":
    verify_infrastructure()
