import { useState, useRef, useEffect } from "react";
import TabContent from "./TabContent";
import StudentConfigService from "../../services/studentConfigService";

export default function FolderTabs() {
    const [tabs, setTabs] = useState<string[]>([]);
    const [activeTab, setActiveTab] = useState(0);
    const [newTabName, setNewTabName] = useState("");
    const [midYearUpdate, setMidYearUpdate] = useState(false);
    const tabsRef = useRef<HTMLDivElement>(null);

    // Fetch years on mount and sort them
    useEffect(() => {
        async function fetchYears() {
            const years = await StudentConfigService.getAllYears();
            const sortedYears = years.sort((a, b) => {
                const [aStart, aEnd] = a.split("-");
                const [bStart, bEnd] = b.split("-");
                if (bStart !== aStart) return parseInt(bStart) - parseInt(aStart);
                const aEndNum = parseInt(aEnd.replace(/\D/g, ""));
                const bEndNum = parseInt(bEnd.replace(/\D/g, ""));
                return bEndNum - aEndNum;
            });
            setTabs(sortedYears);
        }
        fetchYears();
    }, []);

    const addTab = () => {
        if (newTabName.trim() === "") return;
        setTabs([newTabName, ...tabs]);
        setNewTabName("");
    };

    useEffect(() => {
        const handleWheel = (e: WheelEvent) => {
            e.preventDefault();
            if (tabsRef.current) tabsRef.current.scrollLeft += e.deltaY;
        };

        const tabNode = tabsRef.current;
        if (tabNode) tabNode.addEventListener("wheel", handleWheel, { passive: false });
        return () => {
            if (tabNode) tabNode.removeEventListener("wheel", handleWheel);
        };
    }, []);

    return (
        <div style={{ maxWidth: "80%", margin: "0 auto" }}>
            {/* Top controls */}
            <div style={{ display: "flex", gap: "16px", marginBottom: "16px", alignItems: "center" }}>
                <input
                    type="text"
                    placeholder="New tab name"
                    value={newTabName}
                    onChange={(e) => setNewTabName(e.target.value)}
                    style={{ flex: 1, padding: "8px", borderRadius: "4px", border: "1px solid #ccc" }}
                />
                <button
                    onClick={addTab}
                    style={{
                        padding: "8px 16px",
                        backgroundColor: "#517c9e",
                        color: "#fff",
                        border: "none",
                        borderRadius: "4px",
                        cursor: "pointer",
                    }}
                >
                    Add Tab
                </button>
                <label style={{ display: "flex", alignItems: "center", gap: "4px", color: "black" }}>
                    <input
                        type="checkbox"
                        checked={midYearUpdate}
                        onChange={() => setMidYearUpdate(!midYearUpdate)}
                    />
                    Mid Year Update
                </label>
            </div>

            {/* Tab row */}
            <div
                ref={tabsRef}
                style={{
                    display: "flex",
                    borderBottom: "2px solid #ccc",
                    marginBottom: "16px",
                    overflowX: "auto",
                    whiteSpace: "nowrap",
                    overflowY: "hidden",
                    scrollbarWidth: "none",
                    msOverflowStyle: "none",
                }}
            >
                {tabs.map((tab, index) => (
                    <div
                        key={index}
                        onClick={() => setActiveTab(index)}
                        style={{
                            display: "inline-block",
                            padding: "8px 16px",
                            cursor: "pointer",
                            borderTopLeftRadius: "4px",
                            borderTopRightRadius: "4px",
                            backgroundColor: activeTab === index ? "#152255" : "#509cdb",
                            border: activeTab === index ? "2px solid #517c9e" : "2px solid transparent",
                            fontWeight: 600,
                            marginRight: "2px",
                        }}
                    >
                        {tab}
                    </div>
                ))}
            </div>

            {/* Active tab content */}
            <div
                style={{
                    padding: "16px",
                    border: "2px solid #517c9e",
                    borderRadius: "0 4px 4px 4px",
                    backgroundColor: "#152255",
                }}
            >
                {tabs[activeTab] && <TabContent tabName={tabs[activeTab]} />}
            </div>

            {/* Hide scrollbar for Chrome, Safari, Opera */}
            <style>{`
				div::-webkit-scrollbar {
					display: none;
				}
			`}</style>
        </div>
    );
}
