from playwright.sync_api import Page, expect, sync_playwright
import re
import os

def verify_player_history(page: Page):
    # 1. Login
    page.goto("http://localhost:3000/login")
    page.fill("input[placeholder='Username']", "lollito")
    page.fill("input[placeholder='Password']", "ciao")
    page.click("button[type='submit']")

    # Wait for login and redirect to home or dashboard
    page.wait_for_url("http://localhost:3000/")

    # 2. Go to Team page
    page.goto("http://localhost:3000/team")

    # 3. Find a player link and click it
    # We expect links to /player/{id}
    # Let's find the first one.
    player_link = page.locator("a[href^='/player/']").first
    player_link.click()

    # 4. Assert we are on player page
    expect(page).to_have_url(re.compile(r".*/player/\d+"))

    # 5. Check for Player History elements
    # Wait for loading to finish
    expect(page.locator("text=Loading player history...")).not_to_be_visible(timeout=10000)

    expect(page.locator("text=Career Matches")).to_be_visible()
    expect(page.locator("text=Career Goals")).to_be_visible()

    # Check tabs
    expect(page.locator("button:has-text('Overview')")).to_be_visible()
    expect(page.locator("button:has-text('Seasons')")).to_be_visible()

    # 6. Take Screenshot
    page.screenshot(path="verification/player_history.png", full_page=True)

if __name__ == "__main__":
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        try:
            verify_player_history(page)
        except Exception as e:
            print(f"Verification failed: {e}")
            page.screenshot(path="verification/failure.png")
            raise e
        finally:
            browser.close()
