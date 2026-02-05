from playwright.sync_api import sync_playwright, expect
import time

def run():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        # Login via UI
        print("Logging in as lollito...")
        page.goto("http://localhost:3000/login")
        try:
            page.get_by_placeholder("Username").fill("lollito")
            page.get_by_placeholder("Password").fill("ciao")
            page.get_by_role("button", name="Sign in").click()
        except Exception as e:
            print(f"Login form interaction failed: {e}")
            return

        # Wait for home page
        try:
            page.wait_for_url("http://localhost:3000/", timeout=10000)
            print("Logged in.")
        except:
            print("Login failed.")
            return

        # Navigate to Team page
        print("Navigating to Team page...")
        page.goto("http://localhost:3000/team")

        # Wait for content
        try:
            expect(page.get_by_role("heading", name="Team")).to_be_visible(timeout=5000)
        except:
            print("Team heading not found.")
            # print(page.content()) # Too verbose

        # Click Staff tab
        print("Clicking Staff tab...")
        try:
            # Try generic selector
            page.click("text=Staff")
        except Exception as e:
            print(f"Staff tab click failed: {e}")
            page.screenshot(path="verification/no_staff_tab.png")
            return

        # Verify
        print("Verifying UI...")
        try:
            expect(page.get_by_role("heading", name="Staff Management")).to_be_visible()
            # Click Available Staff
            page.click("text=Available Staff")
            # Check for dropdown
            expect(page.locator("select")).to_be_visible()

        except Exception as e:
            print(f"Verification failed: {e}")
            page.screenshot(path="verification/ui_failed.png")
            return

        # Take screenshot
        print("Taking screenshot...")
        time.sleep(2)
        page.screenshot(path="verification/staff_verification.png")
        print("Screenshot saved to verification/staff_verification.png")

        browser.close()

if __name__ == "__main__":
    run()
